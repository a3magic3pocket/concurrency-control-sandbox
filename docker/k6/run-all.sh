#!/bin/bash

# naive 요청 실험, summary
cat ./docker/k6/script-naive.js | docker run --volume "./docker/k6-result:/k6-result" --network concurrency-control-sandbox_app-network --name k6 --rm -i grafana/k6 run -

# pessimistic lock 요청 실험, summary
cat ./docker/k6/script-pessimistic-lock.js | docker run --volume "./docker/k6-result:/k6-result" --network concurrency-control-sandbox_app-network --name k6 --rm -i grafana/k6 run -

# optimistic lock 요청 실험, summary
cat ./docker/k6/script-optimistic-lock.js | docker run --volume "./docker/k6-result:/k6-result" --network concurrency-control-sandbox_app-network --name k6 --rm -i grafana/k6 run -

# script-pessimistic-lock-with-retrieving 요청 실험, summary
cat ./docker/k6/script-pessimistic-lock-with-retrieving.js | docker run --volume "./docker/k6-result:/k6-result" --network concurrency-control-sandbox_app-network --name k6 --rm -i grafana/k6 run -

# script-optimistic-lock-with-retrieving 요청 실험, summary
cat ./docker/k6/script-optimistic-lock-with-retrieving.js | docker run --volume "./docker/k6-result:/k6-result" --network concurrency-control-sandbox_app-network --name k6 --rm -i grafana/k6 run -

# script-pessimistic-lock-with-many-retrieving 요청 실험, summary
cat ./docker/k6/script-pessimistic-lock-with-many-retrieving.js | docker run --volume "./docker/k6-result:/k6-result" --network concurrency-control-sandbox_app-network --name k6 --rm -i grafana/k6 run -

# script-optimistic-lock-with-many-retrieving 요청 실험, summary
cat ./docker/k6/script-optimistic-lock-with-many-retrieving.js | docker run --volume "./docker/k6-result:/k6-result" --network concurrency-control-sandbox_app-network --name k6 --rm -i grafana/k6 run -

# script-redis-lock 요청 실험, summary
cat ./docker/k6/script-redis-lock.js | docker run --volume "./docker/k6-result:/k6-result" --network concurrency-control-sandbox_app-network --name k6 --rm -i grafana/k6 run -

# script-redis-lock 요청 실험, summary
cat ./docker/k6/script-redis-lock-with-retrieving.js | docker run --volume "./docker/k6-result:/k6-result" --network concurrency-control-sandbox_app-network --name k6 --rm -i grafana/k6 run -

# script-redis-lock-with-many-retrieving 요청 실험, summary
cat ./docker/k6/script-redis-lock-with-many-retrieving.js | docker run --volume "./docker/k6-result:/k6-result" --network concurrency-control-sandbox_app-network --name k6 --rm -i grafana/k6 run -