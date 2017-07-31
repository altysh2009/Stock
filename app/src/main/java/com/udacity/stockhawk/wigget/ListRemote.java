package com.udacity.stockhawk.wigget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.ui.MainActivity;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by Home- on 19/04/2017.
 */

public class ListRemote extends RemoteViewsService {
    Context con;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        con = getApplicationContext();
        Log.d("tag", "onGetViewFactory: ");

        return new RemoteViewsFactory() {
            private DecimalFormat dollarFormatWithPlus;
            private DecimalFormat dollarFormat;
            private DecimalFormat percentageFormat;
            private Cursor cursor = null;

            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {
                if (cursor != null)
                    cursor.close();
                final long identityToken = Binder.clearCallingIdentity();
                cursor = con.getContentResolver().query(Contract.Quote.URI,
                        Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                        null, null, Contract.Quote.COLUMN_SYMBOL);
                dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                dollarFormatWithPlus.setPositivePrefix("+$");
                percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
                percentageFormat.setMaximumFractionDigits(2);
                percentageFormat.setMinimumFractionDigits(2);
                percentageFormat.setPositivePrefix("+");
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (cursor != null)
                    cursor.close();
            }

            @Override
            public int getCount() {
                return cursor != null ? cursor.getCount() : 0;
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (cursor.moveToPosition(position)) {
                    RemoteViews views = new RemoteViews(getPackageName(), R.layout.list_item_quote);
                    views.setTextViewText(R.id.symbol, cursor.getString(Contract.Quote.POSITION_SYMBOL));
                    views.setTextViewText(R.id.price, dollarFormat.format(cursor.getFloat(Contract.Quote.POSITION_PRICE)));

                    float rawAbsoluteChange = cursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
                    float percentageChange = cursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);
                    Intent temple = new Intent();
                    temple.putExtra(MainActivity.name, cursor.getString(Contract.Quote.POSITION_SYMBOL));
                    views.setOnClickFillInIntent(R.id.list_item_quote, temple);


                    String change = dollarFormatWithPlus.format(rawAbsoluteChange);
                    String percentage = percentageFormat.format(percentageChange / 100);
                    int changeid;
                    if (rawAbsoluteChange > 0) {
                        //holder.change.setBackgroundResource(R.drawable.percent_change_pill_green);
                        changeid = R.id.change;
                        views.setViewVisibility(R.id.change, View.VISIBLE);
                        views.setViewVisibility(R.id.change_red, View.GONE);
                    } else {
                        //holder.change.setBackgroundResource(R.drawable.percent_change_pill_red);
                        changeid = R.id.change_red;
                        views.setViewVisibility(R.id.change, View.GONE);
                        views.setViewVisibility(R.id.change_red, View.VISIBLE);
                    }

                    if (PrefUtils.getDisplayMode(getApplicationContext())
                            .equals(getApplicationContext().getString(R.string.pref_display_mode_absolute_key))) {
                        views.setTextViewText(changeid, change);

                    } else {
                        views.setTextViewText(changeid, percentage);

                    }

                    return views;
                }
                return null;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.list_item_quote);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
