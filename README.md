
# Elasticsearch Migration Tool (Java)


## Test Cluster Connection

POST /api/test
```json
{
	"endpoint":"https://localhost:9200",
	"username":"xxx",
	"password":"yyy"
}
```

## Copy Index Metadata and data

POST /api/_copy/index/*
```json
{
	"source": {
    	"endpoint": "http://localhost:9210"
	},
	"dest": {
		"endpoint": "https://router.nasuyun.com:9200",
		"username":"xxx",
		"password":"yyy"		
	}
}
```

## Copy cluster template

POST /api/_copy/template/*
```json
{
	"source": {
	    "endpoint": "http://localhost:9210"
	},
	"dest": {
		"endpoint": "https://router.nasuyun.com:9200",
		"username":"xxx",
		"password":"yyy"		
	}
}
```

## Copy cluster pipeline

POST /api/_copy/pipeline/*
```json
{
	"source": {
    "endpoint": "http://localhost:9210"
	},
	"dest": {
		"endpoint": "https://router.nasuyun.com:9200",
		"username":"xxx",
		"password":"yyy"		
	}
}
```

## Cat state 
```json
GET _state
```
