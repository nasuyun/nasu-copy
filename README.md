
# Elasticsearch Migration Tool (Java)


## Test Cluster Connection

```json
POST /api/test
{
	"endpoint":"https://localhost:9200",
	"username":"xxx",
	"password":"yyy"
}
```

## Copy Index Metadata and data

```json
POST /api/_copy/index/*
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

```json
POST /api/_copy/template/*
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

```json
POST /api/_copy/pipeline/*
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
GET _state# nasu-copy
```
