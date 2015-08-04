package com.ysy.ysywb.ui.interfaces;


import android.content.Intent;
import android.support.v4.app.Fragment;
import android.util.SparseIntArray;

import java.util.List;

/**
 * User: ysy
 * Date: 2015/8/4
 */
public class FixedOnActivityResultBugFragment extends Fragment {
    private final SparseIntArray mRequestCodes = new SparseIntArray();

    /**
     * Registers request code (used in
     * {@link #startActivityForResult(Intent, int)}).
     *
     * @param requestCode the request code.
     * @param id          the fragment ID (can be {@link Fragment#getId()} of
     *                    {@link Fragment#hashCode()}).
     */
    public void registerRequestCode(int requestCode, int id) {
        mRequestCodes.put(requestCode, id);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!checkNestedFragmentsForResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        if (getParentFragment() instanceof FixedOnActivityResultBugFragment) {
            ((FixedOnActivityResultBugFragment) getParentFragment())
                    .registerRequestCode(requestCode, hashCode());
        } else {
            super.startActivityForResult(intent, requestCode);
        }
    }

    /**
     * Checks to see whether there is any children fragments which has been
     * registered with {@code requestCode} before. If so, let it handle the
     * {@code requestCode}.
     *
     * @param requestCode the code from {@link #onActivityResult(int, int, Intent)}.
     * @param resultCode  the code from {@link #onActivityResult(int, int, Intent)}.
     * @param data        the data from {@link #onActivityResult(int, int, Intent)}.
     * @return {@code true} if the results have been handed over to some child
     * fragment. {@code false} otherwise.
     */
    protected boolean checkNestedFragmentsForResult(int requestCode,
                                                    int resultCode, Intent data) {
        final int id = mRequestCodes.get(requestCode);
        if (id == 0) {
            return false;
        }

        mRequestCodes.delete(requestCode);
        List<Fragment> fragments = getChildFragmentManager().getFragments();
        if (fragments == null) {
            return false;
        }

        for (Fragment fragment : fragments) {
            if (fragment.hashCode() == id) {
                fragment.onActivityResult(requestCode, resultCode, data);
                return true;
            }
        }
        return false;
    }
}
