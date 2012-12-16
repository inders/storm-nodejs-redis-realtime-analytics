#!/bin/bash
curl -XGET "localhost:9200/ticks/_search?size=0&pretty=true" -d '{
    "query" : {
        "match_all" : {  }
    },
        "facets" : {
            "sentiment" : {
                "terms" : {
                    "field" : "sentiment",
                    "all_terms" : true
                }
            }
        }
}'


