package jp.naotiki_apps.store;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.ArraySet;
import android.util.Log;


import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.core.content.FileProvider;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.net.ftp.FTPClient;

import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.io.CopyStreamAdapter;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class UpdateAppByFTP extends AsyncTask<FTPData,Void,Void> {
    private Context context;
    private List<View> progressViews;
    CopyStreamAdapter streamListener;
    public void setContext(Context mContext){
        context = mContext;
    }
    public void setProgressViews(List<View> views){
        progressViews=views;
    };
    @Override
    protected void onPreExecute(){
        super.onPreExecute();

    }
    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

    }



    @Override
    protected Void doInBackground(FTPData... param) {
        try {
            FTPData data= param[0];

            String serverName       = data.getServerAddress();
            String userName         = data.getUserName();
            String password         = data.getPassword();
            String serverFilePath   = data.getAPKPath();
            File localFile          = data.getLocal();
            if(getFileByFTP(serverName,userName,password,serverFilePath,localFile)){
             /*   Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(new File(localFilePath)), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // without this flag android returned a intent error!
                context.startActivity(intent);*/
            }else{
                //Do nothing could not download
            }/*
            Intent intent1 = new Intent(Intent.ACTION_VIEW);
            intent1.setDataAndType(Uri.fromFile(new File(context.getCacheDir(),apkFileName)), "application/vnd.android.package-archive");
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // without this flag android returned a intent error!
            context.startActivity(intent1);*/
            //String apkFileName="shooting.apk";
            Uri apkUri =
                    FileProvider.getUriForFile(
                            context ,
            BuildConfig.APPLICATION_ID + ".fileprovider", localFile
                    );  // 1
            Intent intent = new Intent(Intent.ACTION_VIEW);   // 2
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)  ;
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) ; // 3
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");  // 4
            context.startActivity(intent);  // 6
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    //Below code to download using FTP
    public  boolean getFileByFTP(String serverName, String userName,
                                 String password, String serverFilePath, File localFile)
            throws Exception {
        FTPClient ftp = new FTPClient();
        try {
            ftp.connect(serverName);
            int reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                return false;
            }
        } catch (IOException e) {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException f) {
                    throw e;
                }
            }
            throw e;
        } catch (Exception e) {
            throw e;
        }
        try {
            if (!ftp.login(userName, password)) {
                ftp.logout();
            }
            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftp.enterLocalPassiveMode();
            final int lenghtOfFile =(int)getFileSize(ftp,serverFilePath);
            OutputStream output = new FileOutputStream(localFile);
            CountingOutputStream cos = new CountingOutputStream(output) {
                protected void beforeWrite(int n) {
                    super.beforeWrite(n);

                    int percent =(int) ((((float)getCount()/1000000)/ ((float)lenghtOfFile/1000000) ) * 100);
                    Log.i("FTP_DOWNLOAD", "bytesTransferred /downloaded"+((float)lenghtOfFile/1000000));
                    Log.i("FTP_DOWNLOAD","Downloaded "+((float)getCount()/1000000) + "/" + ((float)lenghtOfFile/1000000));
                    ((ProgressBar)progressViews.get(0)).setProgress(percent);
                    ((TextView)progressViews.get(1)).setText(((float)getCount()/1000000)+"MB / "+((float)lenghtOfFile/1000000)+"MB");
                }
            };

            ftp.setBufferSize(2024*2048);//To increase the  download speed
            ftp.retrieveFile(serverFilePath, cos);
            output.close();
            ftp.noop(); // check that control connection is working OK
            ftp.logout();
            return true;
        }
        catch (FTPConnectionClosedException e) {
            Log.d("FTP_DOWNLOAD", "ERROR FTPConnectionClosedException:"+e.toString());
            throw e;
        } catch (IOException e) {
            Log.d("FTP_DOWNLOAD", "ERROR IOException:"+e.toString());
            throw e;
        } catch (Exception e) {
            Log.d("FTP_DOWNLOAD", "ERROR Exception:"+e.toString());
            throw e;
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException f) {
                    throw f;
                }
            }
        }
    }
    private static long getFileSize(FTPClient ftp, String filePath) throws Exception {
        long fileSize = 0;
        FTPFile[] files = ftp.listFiles(filePath);
        if (files.length == 1 && files[0].isFile()) {
            fileSize = files[0].getSize();
        }
        Log.d("FTP_DOWNLOAD", "File size = " + fileSize);
        return fileSize;
    }
}