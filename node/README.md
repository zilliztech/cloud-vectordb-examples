## Getting started

### Prerequisites

    Node 14+

### Git clone the example code repo

    git clone https://github.com/zilliztech/cloud-vectordb-examples

### Go to milvus-node folder

    cd cloud-vectordb-examples
    cd node

### Modify uri, user name and user password in configuration file.(config.js)

```javascript
 {
  uri: " https://in01-XXXXXXXXXXXXX.aws-us-west-2.vectordb.zillizcloud.com:XXXXX",
  user: "test",
  password: "123456aa!",
  secure: "True",
};
```

### Run HelloZillizCloud.js

```shell
npm install
node HelloZillizCloud.js
```

### It should print information on the console

```shell
Connecting to DB: https://in01-XXXXXXXXXXXXX.aws-us-west-2.vectordb.zillizcloud.com:XXXXX
Success!
Schema: [
{
name: 'book_id',
description: 'customized primary id',
data_type: 5,
is_primary_key: true,
autoID: false
},
{ name: 'word_count', description: 'word count', data_type: 5 },
{
name: 'book_intro',
description: 'word count',
data_type: 101,
type_params: { dim: 128 }
}
]
Creating example collection: book: 591.987ms
Inserting 100000 entities successfully: 2.428s
Create index successfully: 157.503ms
Load Collection successfully: 1.467s
Searching vector:: 552.828ms
{
status: { error_code: 'Success', reason: '', code: 0 },
results: [
{
score: 14.75003433227539,
id: '64397',
book_id: '64397',
word_count: '842'
}
]
}
```
