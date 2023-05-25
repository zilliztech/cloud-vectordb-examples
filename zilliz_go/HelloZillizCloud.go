package main

import (
	"context"
	"fmt"
	"math/rand"
	"time"

	"github.com/milvus-io/milvus-sdk-go/v2/client"
	"github.com/milvus-io/milvus-sdk-go/v2/entity"
)

func main() {
	// connect to milvus
	fmt.Println("Connecting to DB: " + uri)
	ctx := context.Background()
	Client, err := client.NewDefaultGrpcClientWithURI(ctx, uri, user, password)
	if err != nil {
		fmt.Println("fail to connect to milvus")
	} else {
		fmt.Println("Success!")
	}

	// delete collection if exists
	has, err := Client.HasCollection(ctx, "book")
	if err != nil {
		fmt.Println("no existing collection")
	}
	if has {
		Client.DropCollection(ctx, "book")
	}

	// create a collection
	fmt.Println("Creating example collection: book")
	collectionName := "book"
	schema := &entity.Schema{
		CollectionName: collectionName,
		Description:    "Medium articles published between Jan 2020 to August 2020 in prominent publications",
		AutoID:         false,
		Fields: []*entity.Field{
			{
				Name:        "book_id",
				DataType:    entity.FieldTypeInt64,
				PrimaryKey:  true,
				Description: "customized primary id",
			},
			{
				Name:        "word_count",
				DataType:    entity.FieldTypeInt64,
				Description: "word count",
			},
			{
				Name:     "book_intro",
				DataType: entity.FieldTypeFloatVector,
				TypeParams: map[string]string{
					entity.TypeParamDim: "128",
				},
			},
		},
	}
	err = Client.CreateCollection(ctx, schema, entity.DefaultShardNumber)
	if err != nil {
		fmt.Printf("Fail to create collection")
	} else {
		fmt.Println("Success!")
	}

	// insert data
	fmt.Println("Inserting 100000 entities... ")
	dim := 128
	num_entities := 100000
	idList, countList := make([]int64, 0, num_entities), make([]int64, 0, num_entities)
	vectorList := make([][]float32, 0, num_entities)
	for i := 0; i < num_entities; i++ {
		idList = append(idList, int64(i))
		countList = append(countList, int64(i))
		vec := make([]float32, 0, dim)
		for j := 0; j < dim; j++ {
			vec = append(vec, rand.Float32())
		}
		vectorList = append(vectorList, vec)
	}

	idData := entity.NewColumnInt64("book_id", idList)
	countData := entity.NewColumnInt64("word_count", countList)
	vectorData := entity.NewColumnFloatVector("book_intro", dim, vectorList)

	begin := time.Now()
	_, err = Client.Insert(ctx, collectionName, "", idData, countData, vectorData)
	end := time.Now()
	if err != nil {
		fmt.Println("Fail to insert data")
	} else {
		fmt.Println("Succeed in ", end.Sub(begin))
	}

	// create index
	fmt.Println("Building AutoIndex...")
	index, err := entity.NewIndexAUTOINDEX(entity.L2)
	if err != nil {
		fmt.Printf("fail to get auto index")
	}
	begin = time.Now()
	err = Client.CreateIndex(ctx, collectionName, "book_intro", index, false)
	end = time.Now()
	if err != nil {
		fmt.Printf("fail to create index")
	} else {
		fmt.Println("Succeed in ", end.Sub(begin))
	}

	// load collection
	fmt.Println("Loading collection...")
	begin = time.Now()
	err = Client.LoadCollection(ctx, collectionName, false)
	end = time.Now()
	if err != nil {
		fmt.Printf("fail to load collection")
	} else {
		fmt.Println("Succeed in ", end.Sub(begin))
	}

	// search
	sp, _ := entity.NewIndexAUTOINDEXSearchParam(1)
	vectors := []entity.Vector{entity.FloatVector(vectorList[1])}
	fmt.Println("Search...")
	begin = time.Now()
	searchResult, err := Client.Search(
		ctx,
		collectionName,      // collectionName
		nil,                 // partitionNames
		"",                  // expression
		[]string{"book_id"}, // outputFields
		vectors,             // vectors
		"book_intro",        // vectorField
		entity.L2,           // metricType
		10,                  // topK
		sp,                  // search params
	)
	end = time.Now()
	if err != nil {
		fmt.Println("search failed")
	} else {
		fmt.Println("Succeed in ", end.Sub(begin))
	}
	for _, sr := range searchResult {
		fmt.Println("ids: ", sr.IDs)
		fmt.Println("Scores: ", sr.Scores)
	}
}
