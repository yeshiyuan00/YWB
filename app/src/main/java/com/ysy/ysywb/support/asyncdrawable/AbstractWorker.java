package com.ysy.ysywb.support.asyncdrawable;

import com.ysy.ysywb.support.lib.MyAsyncTask;

/**
 * Created by ggec5486 on 2015/7/23.
 */
public abstract class AbstractWorker<Params, Progress, Result>
        extends MyAsyncTask<Params, Progress, Result>
        implements IPictureWorker {

}
