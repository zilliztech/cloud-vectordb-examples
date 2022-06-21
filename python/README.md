## Getting started

### Prerequisites
    Install Python 3.7+
    pip


### Git clone the example code repo
    git clone https://github.com/zilliztech/cloud-vectordb-examples.git

### Install pymilvus
    pip install pymilvus==2.0.2

### Go to python folder
    cd cloud-vectordb-examples
    cd python

### Modify endpoint and user password in the configuration file, config.ini
    endpoint = https://in01-xxxxxxxxxxxxx.aws-ap-southeast-1.vectordb-sit.zillizcloud.com
    password = ******

### Run hello_milvus.py to run
    Python hello_milvus.py

### It should print information on the console
    Collection info: schema, descrption... 
    partition info: name, description...
    insert count:  2000
    create index: code=0
    search result: ["['(distance: 0.0009647159022279084, id: 47)', '(distance: 0.0012260561343282461, id: 762)']"]
    query count:  4
    ...
    