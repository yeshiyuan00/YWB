package com.ysy.ysywb.ui.login;

import android.app.AlertDialog;
import android.app.Application;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ysy.ysywb.R;
import com.ysy.ysywb.bean.AccountBean;
import com.ysy.ysywb.support.database.AccountDBTask;
import com.ysy.ysywb.support.utils.Utility;
import com.ysy.ysywb.ui.interfaces.AbstractAppActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by ggec5486 on 2015/6/9.
 */
public class AccountActivity extends AbstractAppActivity implements
        LoaderManager.LoaderCallbacks<List<AccountBean>> {

    private static final String ACTION_OPEN_FROM_APP_INNER = "org.qii.weiciyuan:accountactivity";
    private static final String ACTION_OPEN_FROM_APP_INNER_REFRESH_TOKEN = "org.qii.weiciyuan:accountactivity_refresh_token";

    private static final String REFRESH_ACTION_EXTRA = "refresh_account";

    private final int ADD_ACCOUNT_REQUEST_CODE = 0;
    private final int LOADER_ID = 0;

    private ListView listView = null;
    private AccountAdapter listAdapter = null;
    private List<AccountBean> accountList = new ArrayList<AccountBean>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.accountactivity_layout);


        listAdapter = new AccountAdapter();
        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new AccountListItemClickListener());
        listView.setAdapter(listAdapter);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AccountMultiChoiceModeListener());
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_menu_accountactivity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_account:
                showAddAccountDialog();

                break;
        }
        return true;
    }

    private void showAddAccountDialog() {
        final ArrayList<Class> activityList = new ArrayList<Class>();
        ArrayList<String> itemValueList = new ArrayList<String>();

        activityList.add(OAuthActivity.class);
        itemValueList.add(getString(R.string.oauth_login));

        new AlertDialog.Builder(this)
                .setItems(itemValueList.toArray(new String[0]),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(AccountActivity.this,
                                        activityList.get(which));
                                startActivityForResult(intent, ADD_ACCOUNT_REQUEST_CODE);
                            }
                        }).show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADD_ACCOUNT_REQUEST_CODE && resultCode == RESULT_OK) {
            //refresh();
            if (data == null) {
                return;
            }
            String expires_time = data.getExtras().getString("expires_in");
            System.out.println("expires_in=======" + expires_time);
            long expiresDays = TimeUnit.SECONDS.toDays(Long.valueOf(expires_time));
            String content = String.format(getString(R.string.token_expires_in_time),
                    String.valueOf(expiresDays));
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setMessage(content)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            builder.show();
        }
    }

    private void refresh() {
        getLoaderManager().getLoader(LOADER_ID).forceLoad();
    }

    @Override
    public Loader<List<AccountBean>> onCreateLoader(int id, Bundle args) {
        return new AccountDBLoader(AccountActivity.this, args);
    }

    @Override
    public void onLoadFinished(Loader<List<AccountBean>> loader, List<AccountBean> data) {
        accountList = data;
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<List<AccountBean>> loader) {
        accountList = new ArrayList<AccountBean>();
        listAdapter.notifyDataSetChanged();
    }


    private class AccountListItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (!Utility.isTokenValid(accountList.get(position))) {
                showAddAccountDialog();
                return;
            }

        }
    }

    private class AccountMultiChoiceModeListener implements AbsListView.MultiChoiceModeListener {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

        }

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id,
                                              boolean checked) {
            listAdapter.notifyDataSetChanged();
        }
    }

    class AccountAdapter extends BaseAdapter {

        private int checkedBG;
        private int defaultBG;

        public AccountAdapter() {
            defaultBG = getResources().getColor(R.color.transparent);
            checkedBG = getResources().getColor(R.color.saddlebrown);
        }

        @Override
        public int getCount() {
            return accountList.size();
        }

        @Override
        public Object getItem(int position) {
            return accountList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return Long.valueOf(accountList.get(position).getUid());
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null || convertView.getTag() == null) {
                LayoutInflater layoutInflater = getLayoutInflater();
                View mView = layoutInflater.inflate(
                        R.layout.accountactivity_listview_item_layout, parent, false);
                holder = new ViewHolder();
                holder.root = mView.findViewById(R.id.listview_root);
                holder.name = (TextView) mView.findViewById(R.id.account_name);
                holder.avatar = (ImageView) mView.findViewById(R.id.imageView_avatar);
                holder.tokenInvalid = (TextView) mView.findViewById(R.id.token_expired);
                convertView = mView;
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.root.setBackgroundColor(defaultBG);

            if (listView.getCheckedItemPositions().get(position)) {
                holder.root.setBackgroundColor(checkedBG);
            }

            if (accountList.get(position).getInfo() != null) {
                holder.name.setText(accountList.get(position).getInfo().getScreen_name());
            } else {
                getBitmapDownloader()
                        .downloadAvatar(holder.avatar, accountList.get(position).getInfo(), false);
            }

            if (!TextUtils.isEmpty(accountList.get(position).getAvatar_url())) {
                holder.avatar.setImageResource(R.drawable.ic_ysywb);
            }
            holder.tokenInvalid.setVisibility(!Utility.isTokenValid(accountList.get(position)) ? View.VISIBLE : View.GONE);
            return convertView;
        }
    }

    class ViewHolder {
        View root;
        TextView name;
        ImageView avatar;
        TextView tokenInvalid;
    }

    private static class AccountDBLoader extends AsyncTaskLoader<List<AccountBean>> {

        public AccountDBLoader(Context context, Bundle args) {
            super(context);
        }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            forceLoad();
        }

        @Override
        public List<AccountBean> loadInBackground() {
            return AccountDBTask.getAccountList();
        }
    }
}
