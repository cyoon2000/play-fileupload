package models;

import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.*;
import play.Logger;
import play.db.ebean.Model;
import plugins.S3Plugin;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.io.File;
import java.lang.Long;
import java.lang.String;
import java.lang.System;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.UUID;

import com.google.gson.Gson;

import dto.CountResult;
import dto.ReduceData;

@Entity
public class S3File extends Model {

    public static String DOWNLOAD_LOCATION = "/tmp/";

    @Id
    public UUID id;

    private String bucket;

    public String name;

    public String tags;

    public Long userId;

    @Transient
    public File file;

    public static Finder<Long, S3File> find = new Finder<Long, S3File>(Long.class, S3File.class);
    public static Finder<UUID, S3File> findId = new Finder<UUID, S3File>(UUID.class, S3File.class);

    public static S3File findById(UUID id) {
        return
                findId.where()
                        .eq("id", id)
                        .findUnique();
    }

    public static S3File findById(String id) {
        return
                findId.where()
                        .eq("id", UUID.fromString(id))
                        .findUnique();
    }

    public static List<S3File> findByUser(Long userId) {
        return find.where().eq("userId", userId).findList();
    }

    public URL getUrl() throws MalformedURLException {
        return new URL("https://s3.amazonaws.com/" + bucket + "/" + getActualFileName());
    }

    public UUID getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getActualFileName() {
//        return id + "/" + name;
        return id + "-" + name;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    @Override
    public void save() {
        if (S3Plugin.amazonS3 == null) {
            Logger.error("Could not save because amazonS3 was null");
            throw new RuntimeException("Could not save");
        }
        else {
            this.bucket = S3Plugin.s3Bucket;

            super.save(); // assigns an id

            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, getActualFileName(), file);
            putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead); // public for all
            S3Plugin.amazonS3.putObject(putObjectRequest); // upload file
        }
    }

    @Override
    public void delete() {
        if (S3Plugin.amazonS3 == null) {
            Logger.error("Could not delete because amazonS3 was null");
            throw new RuntimeException("Could not delete");
        }
        else {
            S3Plugin.amazonS3.deleteObject(bucket, getActualFileName());
            super.delete();
        }
    }

//    public void updateTags(String tags) {
    public void updateTags(Map tags) {
        Gson gson = new Gson();
        this.tags = gson.toJson(tags);
        super.save();
    }

    public String download() {
        if (S3Plugin.amazonS3 == null) {
            Logger.error("Could not download because amazonS3 was null");
            throw new RuntimeException("Could not download");
        }
        else {
            this.bucket = S3Plugin.s3Bucket;
            File localFile = new File(DOWNLOAD_LOCATION + this.getActualFileName());
            ObjectMetadata object = S3Plugin.amazonS3.getObject(new GetObjectRequest(bucket, getActualFileName()), localFile);
            return this.getId().toString();
        }
    }

//    public InputStream downloadAsStream() {
//        if (S3Plugin.amazonS3 == null) {
//            Logger.error("Could not download because amazonS3 was null");
//            throw new RuntimeException("Could not download");
//        }
//        else {
//            this.bucket = S3Plugin.s3Bucket;
//            return S3Plugin.amazonS3.getObject(new GetObjectRequest(bucket, getActualFileName()));
//        }
//    }

    public String getTag(int n) {

        if (this.tags == null || this.tags.isEmpty()) return "";

        Gson gson = new Gson();
        Map<String, Integer> data = gson.fromJson(this.tags, LinkedHashMap.class);

        if (data == null ) return "";
        List<String> keyList = new ArrayList<String>(data.keySet());

        return keyList.get(n);
    }

//    public String getSecondTag() {
//        return "second";
//    }
//
//    public String getThirdTag() {
//        return "third";
//    }

}