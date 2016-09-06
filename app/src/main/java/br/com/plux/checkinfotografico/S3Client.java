package br.com.plux.checkinfotografico;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import android.os.Handler;
import android.os.Message;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transfermanager.TransferManager;
import com.amazonaws.mobileconnectors.s3.transfermanager.Upload;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gustavonobrega
 */
public class S3Client {

    public AmazonS3 conn;
    AWSCredentials credentials;
    ClientConfiguration clientConfiguration;
    String bucket;

    public S3Client() {
        credentials = new BasicAWSCredentials(App.AWS_ACCESS_KEY_ID, App.AWS_SECRET_KEY);
        clientConfiguration = new ClientConfiguration();
        clientConfiguration.setConnectionTimeout(0);
        this.connect();
    }

    public void connect() {
        conn = new AmazonS3Client(this.credentials, this.clientConfiguration);
    }

    public void synchronizeAsync(String bucket, String prefix) {
        final S3Client s3Client = this;
        this.bucket = bucket;
        Runnable r = new Runnable() {
            public void run() {

            }
        };

        new Thread(r).start();
    }

    /**
     * Lista todos os arquivos
     *
     * @param bucket
     * @param prefix
     * @return
     */
    public List<S3ObjectSummary> listFiles(String bucket, String prefix) {
        ObjectListing listing = conn.listObjects(App.AWS_S3_BUCKET_DEFAULT, prefix);
        List<S3ObjectSummary> summaries = listing.getObjectSummaries();
        List<S3ObjectSummary> lstFiles = new ArrayList<S3ObjectSummary>();;

        for (S3ObjectSummary summarie : summaries) {
            if (!summarie.getKey().endsWith("/")) {
                lstFiles.add(summarie);
            }
        }

        return lstFiles;
    }

    /**
     * Lista todas as pastas
     *
     * @param bucket
     * @param prefix
     * @return
     */
    public List<S3ObjectSummary> listFolders(String bucket, String prefix) {
        ObjectListing listing = conn.listObjects(App.AWS_S3_BUCKET_DEFAULT, prefix);
        List<S3ObjectSummary> summaries = listing.getObjectSummaries();
        List<S3ObjectSummary> lstFolders = new ArrayList<S3ObjectSummary>();;

        for (S3ObjectSummary summarie : summaries) {
            if (summarie.getKey().endsWith("/")) {
                lstFolders.add(summarie);
            }
        }

        return lstFolders;
    }

    /**
     * Lista todos os objetos existentes no bucket
     *
     * @param bucket
     * @param prefix
     * @return
     */
    public List<S3ObjectSummary> listAll(String bucket, String prefix) {
        ObjectListing objectListing = conn.listObjects(
                new ListObjectsRequest().withPrefix(prefix).withBucketName(App.AWS_S3_BUCKET_DEFAULT));
        List<S3ObjectSummary> listObj = objectListing.getObjectSummaries();

        return listObj;
    }

    /**
     * Lista todos os arquivos do servidor
     */
    public void listFilesServer() {

    }

    /**
     * Realiza o download de um arquivo
     *
     * @param bucketName
     * @param key
     * @param fileLocal
     * @return
     */
    public boolean download(String bucketName, String key, String fileLocal) {
        this.connect();

        //Cria o diretório
        int pos = fileLocal.lastIndexOf("/");
        String folder = fileLocal.substring(0, pos);
        //mkdir(folder);

        //Recupera o arquivo do S3
        S3Object object = conn.getObject(new GetObjectRequest(App.AWS_S3_BUCKET_DEFAULT, key));

        InputStream objectData = object.getObjectContent();
        byte[] buffer = new byte[8 * 1024];

        //Compara os dois arquivo
        boolean isEquals = compare(object, new File(fileLocal));

        //Executa o download se for diferente
        if( !isEquals ) {

            try {
                OutputStream output = new FileOutputStream(fileLocal);
                try {
                    int bytesRead;

                    //Recarrega os dados do arquivo
                    object = conn.getObject(new GetObjectRequest(App.AWS_S3_BUCKET_DEFAULT, key));
                    objectData = object.getObjectContent();

                    while ((bytesRead = objectData.read(buffer)) != -1) {
                        output.write(buffer, 0, bytesRead);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(S3Client.class.getName()).log(Level.SEVERE, null, ex);
                    return false;
                } finally {
                    try {
                        output.close();
                    } catch (IOException ex) {
                        Logger.getLogger(S3Client.class.getName()).log(Level.SEVERE, null, ex);
                        return false;
                    }
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(S3Client.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            } finally {
                try {
                    objectData.close();
                } catch (IOException ex) {
                    Logger.getLogger(S3Client.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        return true;
    }

    /**
     * Envia o arquivo para o bucket
     */
    public boolean upload(final File file, final String s3FileKey, Handler handler) {
        TransferManager tm = new TransferManager(credentials);
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
        }
    }

    /**
     * Compara um arquivo do S3 com um local
     */
    public boolean compare(S3Object objectS3, File localFile) {
        long objS3Size = objectS3.getObjectMetadata().getInstanceLength();
        long localFileSize = localFile.length();
        if( objS3Size == localFileSize ) {
            return true;
        } else {
            return false;
        }

    }
}
