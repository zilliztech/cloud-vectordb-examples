package demo;

import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.DataType;
import io.milvus.grpc.MutationResult;
import io.milvus.param.*;
import io.milvus.param.collection.*;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.index.CreateIndexParam;
import util.PropertyFilesUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class HelloZillizVectorDB {
    public static void main(String[] args) {
        // connect to milvus
        final MilvusServiceClient milvusClient = new MilvusServiceClient(
                ConnectParam.newBuilder()
                        .withHost(PropertyFilesUtil.getRunValue("uri"))
                        .withPort(Integer.parseInt(PropertyFilesUtil.getRunValue("port")))
                        .withAuthorization(PropertyFilesUtil.getRunValue("user"), PropertyFilesUtil.getRunValue("password"))
                        .withSecure(true)
                        .build());

        // Check if the collection exists
        String collectionName = "book";
        R<Boolean> bookR = milvusClient.hasCollection(HasCollectionParam.newBuilder()
                .withCollectionName(collectionName).build());
        if (bookR.getData()) {
            R<RpcStatus> dropR = milvusClient.dropCollection(DropCollectionParam.newBuilder()
                    .withCollectionName("book").build());
            System.out.println("Collection " + collectionName + " is existed,Drop collection: " + dropR.getData().getMsg());
        }

        // create a collection with customized primary field: book_id_field
        int dim = 128;
        FieldType bookIdField = FieldType.newBuilder()
                .withName("book_id")
                .withDataType(DataType.Int64)
                .withPrimaryKey(true)
                .withAutoID(false)
                .build();
        FieldType wordCountField = FieldType.newBuilder()
                .withName("word_count")
                .withDataType(DataType.Int64)
                .build();
        FieldType bookIntroField = FieldType.newBuilder()
                .withName("book_intro")
                .withDataType(DataType.FloatVector)
                .withDimension(dim)
                .build();
        CreateCollectionParam createCollectionParam = CreateCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .withDescription("Test book search")
                .withShardsNum(2)
                .addFieldType(bookIdField)
                .addFieldType(wordCountField)
                .addFieldType(bookIntroField)
                .build();
        milvusClient.createCollection(createCollectionParam);

        //insert data with customized ids
        Random ran = new Random();
        int singleNum = 10000;
        int insertRounds = 10;
        long insertTotalTime = 0L;
        for (int r = 0; r < insertRounds; r++) {
            List<Long> book_id_array = new ArrayList<>();
            List<Long> word_count_array = new ArrayList<>();
            List<List<Float>> book_intro_array = new ArrayList<>();
            for (long i = r * singleNum; i < (r + 1) * singleNum; ++i) {
                book_id_array.add(i);
                word_count_array.add(i + 10000);
                List<Float> vector = new ArrayList<>();
                for (int k = 0; k < dim; ++k) {
                    vector.add(ran.nextFloat());
                }
                book_intro_array.add(vector);
            }
            List<InsertParam.Field> fields = new ArrayList<>();
            fields.add(new InsertParam.Field(bookIdField.getName(), DataType.Int64, book_id_array));
            fields.add(new InsertParam.Field(wordCountField.getName(), DataType.Int64, word_count_array));
            fields.add(new InsertParam.Field(bookIntroField.getName(), DataType.FloatVector, book_intro_array));
            InsertParam insertParam = InsertParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withFields(fields)
                    .build();
            long startTime = System.currentTimeMillis();
            R<MutationResult> insertR = milvusClient.insert(insertParam);
            long endTime = System.currentTimeMillis();
            insertTotalTime += (endTime - startTime) / 1000.0;
        }
        System.out.println("totally insert " + singleNum * insertRounds + " entities cost " + insertTotalTime + " seconds");

        // build index
        final IndexType INDEX_TYPE = IndexType.HNSW;   // IndexType
        final String INDEX_PARAM = "{\"M\":16,\"efConstruction\":64}";     // ExtraParam
        long startIndexTime = System.currentTimeMillis();
        R<RpcStatus> indexR = milvusClient.createIndex(
                CreateIndexParam.newBuilder()
                        .withCollectionName(collectionName)
                        .withFieldName(bookIntroField.getName())
                        .withIndexType(INDEX_TYPE)
                        .withMetricType(MetricType.L2)
                        .withExtraParam(INDEX_PARAM)
                        .withSyncMode(Boolean.TRUE)
                        .withSyncWaitingInterval(500L)
                        .withSyncWaitingTimeout(30L)
                        .build());
        long endIndexTime = System.currentTimeMillis();
        System.out.println("collection " + collectionName + " build index in " + (endIndexTime - startIndexTime) / 1000.0 + " seconds");

        // load collection
        long startLoadTime = System.currentTimeMillis();
        milvusClient.loadCollection(LoadCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .withSyncLoad(true)
                .withSyncLoadWaitingInterval(500L)
                .withSyncLoadWaitingTimeout(30L)
                .build());
        long endLoadTime = System.currentTimeMillis();
        System.out.println("collection " + collectionName + " load in " + (endLoadTime - startLoadTime) / 1000.0 + " seconds");

        // search
        final Integer SEARCH_K = 2;                       // TopK
        final String SEARCH_PARAM = "{\"nprobe\":10}";    // Params
        List<String> search_output_fields = Arrays.asList("book_id", "word_count");
        for (int i = 0; i < 10; i++) {
            List<Float> floatList = new ArrayList<>();
            for (int k = 0; k < dim; ++k) {
                floatList.add(ran.nextFloat());
            }
            List<List<Float>> search_vectors = Arrays.asList(floatList);
            SearchParam searchParam = SearchParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withMetricType(MetricType.L2)
                    .withOutFields(search_output_fields)
                    .withTopK(SEARCH_K)
                    .withVectors(search_vectors)
                    .withVectorFieldName(bookIntroField.getName())
                    .withParams(SEARCH_PARAM)
                    .build();
            long startSearchTime=System.currentTimeMillis();
            milvusClient.search(searchParam);
            long endSearchTime=System.currentTimeMillis();
            System.out.println("search "+i+" latency: "+(endSearchTime-startSearchTime)/1000.0+" seconds");
        }

        milvusClient.close();
        System.out.println("Competed");
    }

}
