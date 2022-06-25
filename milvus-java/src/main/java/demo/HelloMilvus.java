package demo;

import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.*;
import io.milvus.param.*;
import io.milvus.param.collection.*;
import io.milvus.param.dml.DeleteParam;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.QueryParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.param.partition.CreatePartitionParam;
import io.milvus.response.DescCollResponseWrapper;
import io.milvus.response.QueryResultsWrapper;
import io.milvus.response.SearchResultsWrapper;
import util.PropertyFilesUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class HelloMilvus {
    public static void main(String[] args) {
        /**
         * step: connect milvus
         * parameters: host, port, username, password, secure=true
         * return: MilvusClient
         * */
        final MilvusServiceClient milvusClient = new MilvusServiceClient(
                ConnectParam.newBuilder()
                        .withHost(PropertyFilesUtil.getRunValue("endpoint"))
                        .withPort(Integer.parseInt(PropertyFilesUtil.getRunValue("port")))
                        .withAuthorization(PropertyFilesUtil.getRunValue("user"), PropertyFilesUtil.getRunValue("password"))
                        .withSecure(true)
                        .build());

        /***
         *  step: check collection is existed or not
         *  parameters: collection name
         *  desc: drop collection before create
         */
        R<Boolean> bookR = milvusClient.hasCollection(HasCollectionParam.newBuilder()
                .withCollectionName("book").build());
        if (bookR.getData()){
            R<RpcStatus> dropR = milvusClient.dropCollection(DropCollectionParam.newBuilder()
                    .withCollectionName("book").build());
            System.out.println("********************Collection is existed,Drop collection: " + dropR.getData().getMsg() + "********************");
        }

        /**
         * step: create collection
         * parameters: primary key field, scalar field, vector field, collection name
         * return: collection
         * */
        FieldType fieldType1 = FieldType.newBuilder()
                .withName("book_id")
                .withDataType(DataType.Int64)
                .withPrimaryKey(true)
                .withAutoID(false)
                .build();
        FieldType fieldType2 = FieldType.newBuilder()
                .withName("word_count")
                .withDataType(DataType.Int64)
                .build();
        FieldType fieldType3 = FieldType.newBuilder()
                .withName("book_intro")
                .withDataType(DataType.FloatVector)
                .withDimension(2)
                .build();
        CreateCollectionParam createCollectionParam = CreateCollectionParam.newBuilder()
                .withCollectionName("book")
                .withDescription("Test book search")
                .withShardsNum(2)
                .addFieldType(fieldType1)
                .addFieldType(fieldType2)
                .addFieldType(fieldType3)
                .build();
        R<RpcStatus> collectionR = milvusClient.createCollection(createCollectionParam);
        System.out.println("********************Create collection:" + collectionR.getData().getMsg() + "********************");
        /**
         * step: create partition
         * parameters: collection name, partition name
         * */
        R<RpcStatus> partitionR = milvusClient.createPartition(
                CreatePartitionParam.newBuilder()
                        .withCollectionName("book")
                        .withPartitionName("book_part")
                        .build());
        System.out.println("********************Create partition: " + partitionR.getData().getMsg() + "********************");

        /**
         * step: query collection info
         * parameters: collection name
         * return: information and schema of the collection
         * */
        R<DescribeCollectionResponse> respDescribeCollection = milvusClient.describeCollection(          // Return the name and schema of the collection.
                DescribeCollectionParam.newBuilder()
                        .withCollectionName("book")
                        .build());
        DescCollResponseWrapper wrapperDescribeCollection = new DescCollResponseWrapper(respDescribeCollection.getData());
        System.out.println("********************Collection info:" + wrapperDescribeCollection + "********************");

        /**
         * step: insert entities into collection
         * parameters:  List<InsertParam.Field>, CollectionName
         * return: rows of insert entities
         * */
        Random ran = new Random();
        List<Long> book_id_array = new ArrayList<>();
        List<Long> word_count_array = new ArrayList<>();
        List<List<Float>> book_intro_array = new ArrayList<>();
        for (long i = 0L; i < 2000; ++i) {
            book_id_array.add(i);
            word_count_array.add(i + 10000);
            List<Float> vector = new ArrayList<>();
            for (int k = 0; k < 2; ++k) {
                vector.add(ran.nextFloat());
            }
            book_intro_array.add(vector);
        }
        List<InsertParam.Field> fields = new ArrayList<>();
        fields.add(new InsertParam.Field("book_id", DataType.Int64, book_id_array));
        fields.add(new InsertParam.Field("word_count", DataType.Int64, word_count_array));
        fields.add(new InsertParam.Field("book_intro", DataType.FloatVector, book_intro_array));

        InsertParam insertParam = InsertParam.newBuilder()
                .withCollectionName("book")
                .withFields(fields)
                .build();
        R<MutationResult> insertR = milvusClient.insert(insertParam);
        System.out.println("********************Insert success rows: " + insertR.getData().getSuccIndexCount() + "********************");

        /**
         * step: create index
         * parameters: IndexType, INDEX_PARAM, MetricType, FieldName, CollectionName
         * */
        final IndexType INDEX_TYPE = IndexType.IVF_FLAT;   // IndexType
        final String INDEX_PARAM = "{\"nlist\":1024}";     // ExtraParam
        R<RpcStatus> indexR = milvusClient.createIndex(
                CreateIndexParam.newBuilder()
                        .withCollectionName("book")
                        .withFieldName("book_intro")
                        .withIndexType(INDEX_TYPE)
                        .withMetricType(MetricType.L2)
                        .withExtraParam(INDEX_PARAM)
                        .withSyncMode(Boolean.FALSE)
                        .build());
        System.out.println("********************Create index: " + indexR.getData().getMsg() + "********************");

        /***
         * step: load collection into memory
         * parameter: collection name
         * desc:All search and query operations within Milvus are executed in memory.
         *      Load the collection to memory before conducting a vector search.
         */
        R<RpcStatus> loadCollectionR = milvusClient.loadCollection(
                LoadCollectionParam.newBuilder()
                        .withCollectionName("book")
                        .build());
        System.out.println("********************Load collection:" + loadCollectionR.getData().getMsg() + "********************");


        /**
         * step: conduct a hybrid vector search
         * parameters: collectionName, metricType, outFields, Topk, vectors, vectorFieldName
         * return: book_id field and word_count of the results
         * */
        final Integer SEARCH_K = 2;                       // TopK
        final String SEARCH_PARAM = "{\"nprobe\":10}";    // Params
        List<String> search_output_fields = Arrays.asList("book_id", "word_count");
        List<List<Float>> search_vectors = Arrays.asList(Arrays.asList(0.1f, 0.2f));

        SearchParam searchParam = SearchParam.newBuilder()
                .withCollectionName("book")
                .withMetricType(MetricType.L2)
                .withOutFields(search_output_fields)
                .withTopK(SEARCH_K)
                .withVectors(search_vectors)
                .withVectorFieldName("book_intro")
                .withParams(SEARCH_PARAM)
                .withExpr("word_count >= 11000")
                .build();
        R<SearchResults> respSearchR = milvusClient.search(searchParam);
        SearchResultsWrapper searchResultsWrapper = new SearchResultsWrapper(respSearchR.getData().getResults());
        System.out.println("********************Search book_id result: " + searchResultsWrapper.getFieldData("book_id", 0) + "********************");
        System.out.println("********************Search word_count result: " + searchResultsWrapper.getFieldData("word_count", 0) + "********************");

        /**
         * step: conduct a vector query
         * parameters: expression, collection name ,out fields
         * return: book_id field and word_count of the results
         * */
        List<String> query_output_fields = Arrays.asList("book_id", "word_count");
        QueryParam queryParam = QueryParam.newBuilder()
                .withCollectionName("book")
                .withExpr("book_id in [2,4,6,8]")
                .withOutFields(query_output_fields)
                .build();
        R<QueryResults> respQuery = milvusClient.query(queryParam);
        QueryResultsWrapper wrapperQuery = new QueryResultsWrapper(respQuery.getData());
        System.out.println("********************Query book_id result: " + wrapperQuery.getFieldWrapper("book_id").getFieldData() + "********************");
        System.out.println("********************Query word_count result: " + wrapperQuery.getFieldWrapper("word_count").getFieldData() + "********************");

        /**
         * step: delete entities
         * params: delete expression, collection name
         * return: rows of delete success
         * */
        R<MutationResult> deleteR = milvusClient.delete(
                DeleteParam.newBuilder()
                        .withCollectionName("book")
                        .withExpr("book_id in [0,1]")
                        .build());
        System.out.println("********************Delete success count: " + deleteR.getData().getDeleteCnt() + "********************");

        /**
         * step: release collection
         * parameters: collection name
         * desc: release a collection from memory
         * */
        R<RpcStatus> releaseR = milvusClient.releaseCollection(
                ReleaseCollectionParam.newBuilder()
                        .withCollectionName("book")
                        .build());
        System.out.println("********************Release collection: " + releaseR.getData().getMsg() + "********************");


        /**
         * step: drop collection
         * parameters: collection name
         * caution: Dropping a collection irreversibly deletes all data within it.
         * */
        R<RpcStatus> dropR = milvusClient.dropCollection(
                DropCollectionParam.newBuilder()
                        .withCollectionName("book")
                        .build());
        System.out.println("********************Drop collection: " + dropR.getData().getMsg() + "********************");

        /**
         * step: close connect
         * */
        milvusClient.close();
    }
}
