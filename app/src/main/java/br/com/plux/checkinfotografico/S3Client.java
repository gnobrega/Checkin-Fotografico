package br.com.plux.checkinfotografico;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.File;

/**
 *
 * @author gustavonobrega
 */
public class S3Client {

    private static AmazonS3Client sS3Client;
    private static CognitoCachingCredentialsProvider sCredProvider;
    private static TransferUtility sTransferUtility;
    private Handler handler;

    /**
     * Gets an instance of CognitoCachingCredentialsProvider which is
     * constructed using the given Context.
     *
     * @param context An Context instance.
     * @return A default credential provider.
     */
    private static CognitoCachingCredentialsProvider getCredProvider(Context context) {
        if (sCredProvider == null) {
            sCredProvider = new CognitoCachingCredentialsProvider(
                    context.getApplicationContext(),
                    App.COGNITO_POOL_ID,
                    Regions.US_EAST_1);
        }
        return sCredProvider;
    }

    /**
     * Gets an instance of a S3 client which is constructed using the given
     * Context.
     *
     * @param context An Context instance.
     * @return A default S3 client.
     */
    public static AmazonS3Client getS3Client(Context context) {
        if (sS3Client == null) {
            sS3Client = new AmazonS3Client(getCredProvider(context.getApplicationContext()));
        }
        return sS3Client;
    }

    /**
     * Gets an instance of the TransferUtility which is constructed using the
     * given Context
     *
     * @param context
     * @return a TransferUtility instance
     */
    public static TransferUtility getTransferUtility(Context context) {
        if (sTransferUtility == null) {
            sTransferUtility = new TransferUtility(getS3Client(context.getApplicationContext()),
                    context.getApplicationContext());
        }

        return sTransferUtility;
    }

    /**
     * Envia o arquivo para o bucket
     */
    public boolean upload(final File file, final String s3FileKey, Handler handler, Context context, String imageKey) {
        this.handler = handler;
        TransferUtility transferUtility;
        transferUtility = S3Client.getTransferUtility(context);
        if( file.exists() ) {
            TransferObserver observer = transferUtility.upload(App.AWS_S3_BUCKET_DEFAULT, s3FileKey, file);
            UploadListener listener = new UploadListener();
            listener.setImageKey(imageKey);
            observer.setTransferListener(listener);
        }

        /*TransferManager tm = new TransferManager(credentials);
        Upload upload = tm.upload(App.AWS_S3_BUCKET_DEFAULT, s3FileKey, file);

        int lastVal = 0;
        while (upload.isDone() == false) {
            if( (int)upload.getProgress().getPercentTransferred() != lastVal ) {
                lastVal = (int)upload.getProgress().getPercentTransferred();
                Message message = new Message();
                message.obj = lastVal;
                handler.sendMessage(message);
            }
        }

        //Upload finalizado
        if( upload.isDone() ) {
            return true;
        } else {
            return false;
        }*/

        return false;
    }

    /**
     * Exibe o progresso do eventos
     */
    protected void setProgress(Long progress, String imageKey) {
        Intent intent = new Intent();

        intent.putExtra("progress", progress);
        intent.putExtra("imageKey", imageKey);
        intent.putExtra("TESTE", "ABCDEF");

        Message message = new Message();
        message.obj = intent;

        handler.sendMessage(message);
    }

    /**********************************************************************************************/

    /*
     * A TransferListener class that can listen to a upload task and be notified
     * when the status changes.
     */
    private class UploadListener implements TransferListener {

        String imageKey = null;

        public void setImageKey(String imageKey) {
            this.imageKey = imageKey;
        }

        // Simply updates the UI list when notified.
        @Override
        public void onError(int id, Exception e) {
            Log.e("LOG", "Error during upload: " + id, e);
        }

        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
            Long progress = Long.valueOf(0);
            if( bytesCurrent > 0 ) {
                progress = bytesCurrent / bytesCurrent * 100;
            }
            setProgress(progress, null);

            Log.d("LOG", String.format("onProgressChanged: %d, total: %d, current: %d, percent: %s", id, bytesTotal, bytesCurrent, progress.toString()));
        }

        @Override
        public void onStateChanged(int id, TransferState newState) {
            Log.d("LOG", "onStateChanged: " + id + ", " + newState);
            if( newState.toString().equals("COMPLETED") ) {
                Long progress = Long.valueOf(100);
                setProgress(progress, this.imageKey);
            }
        }
    }
}
