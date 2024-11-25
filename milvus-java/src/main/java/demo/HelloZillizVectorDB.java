package demo;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.ConsistencyLevel;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.DescribeCollectionReq;
import io.milvus.v2.service.collection.request.DropCollectionReq;
import io.milvus.v2.service.collection.request.LoadCollectionReq;
import io.milvus.v2.service.collection.response.DescribeCollectionResp;
import io.milvus.v2.service.index.request.CreateIndexReq;
import io.milvus.v2.service.utility.request.FlushReq;
import io.milvus.v2.service.vector.request.InsertReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.data.BaseVector;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.InsertResp;
import io.milvus.v2.service.vector.response.SearchResp;
import util.PropertyFilesUtil;

import java.util.*;


public class HelloZillizVectorDB {
    public static void main(String[] args) {
        // connect to milvus
        final MilvusClientV2 milvusClientV2 = new MilvusClientV2(ConnectConfig.builder()
                .uri(PropertyFilesUtil.getRunValue("uri"))
                .token(PropertyFilesUtil.getRunValue("token"))
                .secure(false)
                .connectTimeoutMs(5000L)
                .build());
        System.out.println("Connecting to DB: " + PropertyFilesUtil.getRunValue("uri"));
        // Check if the collection exists
        String collectionName = "book";
        DescribeCollectionResp describeCollectionResp = null;
        try {
            describeCollectionResp = milvusClientV2.describeCollection(DescribeCollectionReq.builder().collectionName(collectionName).build());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        if (describeCollectionResp != null) {
            milvusClientV2.dropCollection(DropCollectionReq.builder().collectionName(collectionName).build());
        }
        System.out.println("Success!");

        // create a collection with customized primary field: book_id
        int dim = 64;
        CreateCollectionReq.FieldSchema bookIdField = CreateCollectionReq.FieldSchema.builder()
                .autoID(false)
                .dataType(io.milvus.v2.common.DataType.Int64)
                .isPrimaryKey(true)
                .name("book_id")
                .build();
        CreateCollectionReq.FieldSchema wordCountField = CreateCollectionReq.FieldSchema.builder()
                .dataType(io.milvus.v2.common.DataType.Int64)
                .name("word_count")
                .isPrimaryKey(false)
                .build();
        CreateCollectionReq.FieldSchema bookIntroField = CreateCollectionReq.FieldSchema.builder()
                .dataType(io.milvus.v2.common.DataType.FloatVector)
                .name("book_intro")
                .isPrimaryKey(false)
                .dimension(dim)
                .build();
        List<CreateCollectionReq.FieldSchema> fieldSchemaList = new ArrayList<>();
        fieldSchemaList.add(bookIdField);
        fieldSchemaList.add(wordCountField);
        fieldSchemaList.add(bookIntroField);
        CreateCollectionReq.CollectionSchema collectionSchema = CreateCollectionReq.CollectionSchema.builder()
                .fieldSchemaList(fieldSchemaList)
                .build();
        System.out.println("Creating example collection: " + collectionName);
        System.out.println("Schema: " + collectionSchema);
        CreateCollectionReq createCollectionReq = CreateCollectionReq.builder()
                .collectionSchema(collectionSchema)
                .collectionName(collectionName)
                .enableDynamicField(false)
                .description("create collection demo")
                .numShards(1)
                .build();
        milvusClientV2.createCollection(createCollectionReq);
        System.out.println("Success!");

        //insert data with customized ids
        Random ran = new Random();
        Gson gson = new Gson();
        int singleNum = 1000;
        int insertRounds = 2;
        long insertTotalTime = 0L;
        System.out.println("Inserting " + singleNum * insertRounds + " entities... ");
        for (int r = 0; r < insertRounds; r++) {
            List<JsonObject> jsonList = new ArrayList<>();
            for (long i = r * singleNum; i < (r + 1) * singleNum; ++i) {
                JsonObject row = new JsonObject();
                row.addProperty(bookIdField.getName(), i);
                row.addProperty(wordCountField.getName(), i + 10000);
                List<Float> vector = new ArrayList<>();
                for (int k = 0; k < dim; ++k) {
                    vector.add(ran.nextFloat());
                }
                row.add(bookIntroField.getName(), gson.toJsonTree(vector));
                jsonList.add(row);
            }
            long startTime = System.currentTimeMillis();
            InsertResp insert = milvusClientV2.insert(InsertReq.builder()
                    .collectionName(collectionName)
                    .data(jsonList).build());
            long endTime = System.currentTimeMillis();
            insertTotalTime += (endTime - startTime) / 1000.00;
        }
        System.out.println("Succeed in " + insertTotalTime + " seconds!");

        // flush data
        System.out.println("Flushing...");
        long startFlushTime = System.currentTimeMillis();
        milvusClientV2.flush(FlushReq.builder().collectionNames(Collections.singletonList(collectionName)).build());
        long endFlushTime = System.currentTimeMillis();
        System.out.println("Succeed in " + (endFlushTime - startFlushTime) / 1000.00 + " seconds!");

        // build index
        System.out.println("Building AutoIndex...");
        IndexParam indexParam = IndexParam.builder()
                .fieldName(bookIntroField.getName())
                .indexType(IndexParam.IndexType.AUTOINDEX)
                .metricType(IndexParam.MetricType.L2)
                .build();
        long startIndexTime = System.currentTimeMillis();
        milvusClientV2.createIndex(CreateIndexReq.builder()
                .collectionName(collectionName)
                .indexParams(Collections.singletonList(indexParam))
                .build());
        long endIndexTime = System.currentTimeMillis();
        System.out.println("Succeed in " + (endIndexTime - startIndexTime) / 1000.00 + " seconds!");

        // load collection
        System.out.println("Loading collection...");
        long startLoadTime = System.currentTimeMillis();
        milvusClientV2.loadCollection(LoadCollectionReq.builder()
                .collectionName(collectionName)
                .async(false)
                .build());
        long endLoadTime = System.currentTimeMillis();
        System.out.println("Succeed in " + (endLoadTime - startLoadTime) / 1000.00 + " seconds");

        // search
        final Integer SEARCH_K = 2;                       // TopK
        Map<String, Object> searchLevel = new HashMap<>(); // Params
        searchLevel.put("level", 1);
        List<String> search_output_fields = Arrays.asList("book_id", "word_count");
        for (int i = 0; i < 10; i++) {
            List<BaseVector> data = new ArrayList<>();
            List<Float> floatList = new ArrayList<>();
            for (int k = 0; k < dim; ++k) {
                floatList.add(ran.nextFloat());
            }
            data.add(new FloatVec(floatList));
            List<List<Float>> search_vectors = Collections.singletonList(floatList);

            long startSearchTime = System.currentTimeMillis();
            SearchResp search = milvusClientV2.search(SearchReq.builder()
                    .data(data)
                    .consistencyLevel(ConsistencyLevel.STRONG)
                    .collectionName(collectionName)
                    .searchParams(searchLevel)
                    .outputFields(search_output_fields)
                    .metricType(IndexParam.MetricType.L2)
                    .topK(SEARCH_K)
                    .build());
            long endSearchTime = System.currentTimeMillis();
            System.out.println("Searching vector: " + search_vectors);
            System.out.println("Result: " + search.getSearchResults());
            System.out.println("search " + i + " latency: " + (endSearchTime - startSearchTime) / 1000.00 + " seconds");
        }

        milvusClientV2.close();
    }

}
