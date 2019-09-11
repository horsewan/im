package com.dy.zserver.utils;

import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

//mongodb 连接数据库工具类
public class MongoUtil {

    public static MongoDatabase m_MongoDatabase = null;
    //不通过认证获取连接数据库对象
    public static MongoDatabase getConnect(){

        if(m_MongoDatabase == null ){
            try{
                //连接到 mongodb 服务
                MongoClient mongoClient = new MongoClient("localhost", 27017);
                //连接到数据库
                MongoClientOptions.Builder options = new MongoClientOptions.Builder();
                options.cursorFinalizerEnabled(true);
                options.connectionsPerHost(1000);
                options.connectTimeout(30000);
                options.maxWaitTime(5000);
                options.socketTimeout(0);
                options.threadsAllowedToBlockForConnectionMultiplier(5000);
                options.writeConcern(WriteConcern.SAFE);
                options.build();
                m_MongoDatabase = mongoClient.getDatabase("dy");
                System.out.println("初始化数据库成功......");
            }catch (Exception e){
                System.out.println("初始化数据库有误......");
                e.printStackTrace();
            }

        }
        //返回连接数据库对象
        return m_MongoDatabase;
    }

    //需要密码认证方式连接
    public static MongoDatabase getConnect2(){
        List<ServerAddress> adds = new ArrayList<>();
        //ServerAddress()两个参数分别为 服务器地址 和 端口
        ServerAddress serverAddress = new ServerAddress("localhost", 27017);
        adds.add(serverAddress);
        List<MongoCredential> credentials = new ArrayList<>();
        //MongoCredential.createScramSha1Credential()三个参数分别为 用户名 数据库名称 密码
        MongoCredential mongoCredential = MongoCredential.createScramSha1Credential("username", "databaseName", "password".toCharArray());
        credentials.add(mongoCredential);
        //通过连接认证获取MongoDB连接
        MongoClient mongoClient = new MongoClient(adds, credentials);
        //连接到数据库
        MongoDatabase mongoDatabase = mongoClient.getDatabase("test");
        //返回连接数据库对象
        return mongoDatabase;
    }

    public static void insertOffLineMsg(String user_id,String from_user_id,String typeu,String fingerPrint,String dataContent){
        try{
            MongoDatabase mongoDatabase = MongoUtil.getConnect();
            MongoCollection<Document> collection = mongoDatabase.getCollection("dy");
            Document document = new Document("user_id",user_id)
                 .append("user_friend_id",from_user_id).append("typeu",typeu).append("fingerPrint",fingerPrint).append("dataContent",dataContent)
                 .append("flag",0);
            collection.insertOne(document);
        }catch (Exception e){
            //e.printStackTrace();
        }
    }

}
