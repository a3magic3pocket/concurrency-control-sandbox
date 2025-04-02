# concurrency-control-sandbox
동시성 제어하는 방법을 실험하고 효율을 확인합니다.

## 빌드
- 그래들 빌드를 진행하면 ./build/libs/demo-0.0.1-SNAPSHOT.jar 파일이 생성됩니다.
- 이 파일은 k6 테스트 진행 시 사용하므로 k6 테스트 전에 미리 생성합니다.

## 테스트
- h2 DB를 사용하여 테스트할 시 OptimisticLockingFailureException를 발생시키는 것이 어렵기 때문에  
  k6 테스트 환경에서 테스트 코드도 테스트 합니다.
- 이를 위해 k6 테스트 도커 컨테이너를 실행시킨 뒤에 테스트를 진행합니다.

## mac 에서 k6 테스트 환경 구축
- ```bash
    # 현재 VM 확인
    colima list 
  
    # colima 현재 VM 삭제
    colima delete
  
    # colima cpu 4개 memory 8gb VM 생성
    colima start --cpu 4 --memory 8

    # 프로젝트 루트 경로로 이동
    cd /some/your/path/concurrency-control-sandbox
  
    # 테스트 없이 빌드
    ./gradlew clean build -x test

    # k6 테스트 도커 컨테이너 실행
    bash ./docker/restart.sh

    # 테스트 실행
    ./gradlew test
    ```

## k6 테스트
- k6 테스트 설명
  - 가정
    - POST 이라는 테이블에 numLikes 필드가 있습니다.
    - likePost 함수는 postId를 인자로 받아  
      post 조회한 뒤 기존 POST.numLikes 에 +1을 하여 저장합니다.
    - 이때 두 개의 트랜잭션이 동시에 likePost(postId = 1)을 호출하면     
      둘 중 하나의 갱신 결과가 소실되는 문제가 발생합니다.
    - 업데이트 소실을 가장 효율적으로 방지할 수 있는 방법을 알아봅니다.
  - 방법
    - pessimistic lock
      - DB 락을 활용하여 막습니다.
    - optimistic lock
      - POST 테이블에 version 필드를 두고 한 트랜잭션 내에서 같은 version 을 가진 행만  
        수정할 수 있게 합니다.
      - 이를 Spring 에서 관리하여 실패 시 OptimisticLockException 을 발생시킵니다.
    - 단일 스레드 비동기 배치 처리 구현
      - redis와 Spring 스케쥴러를 활용하여, 단일 스레드 비동기 배치 처리 구현합니다.
      - 단일 스레드를 활용하여 경쟁 상태에 놓이는 걸 막습니다.
      - 배치 처리를 하여 작업 효율을 올립니다.
  - 실험 환경
    - naive
      - 기본 문자열만 리턴하는 컨트롤러를 조회하는 태스크입니다.
      - 실험의 대조군으로 활용하며 테스트 종료 시간이 가장 짧습니다.
    - {방법명}
      - likePost 만 호출하는 태스크입니다.
    - {방법명}-with-retrieving
      - 모든 POST 조회와 likePost 를 1대1로 호출합니다.
    - {방법명}-with-many-retrieving
      - 모든 POST 조회와 likePost 를 9대1로 호출합니다.
- k6 테스트 시작
  - 모든 테스트 실행
    - ```bash
        ## 모든 k6 실험 실행
        bash ./docker/k6/run-all.sh
        ```
  - 개별 테스트 실행
    - ```bash
      ## k6 실험 개별 실행
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
      ```

- k6 테스트 결과 위치
  - /docker/k6-result

- k6 테스트 결과 summary 설명
  - http_req_failed(http 요청 실패율)
    - rate는 0~100까지이며 0이면 실패 요청이 없다는 뜻입니다.
    - passes가 요청 실패 수, fails가 요청 성공 수입니다.
  - http_reqs (총 요청 수 및 초당 요청 수)
    - count가 총 요청 수
    - rate가 초당 요청 수 입니다.
  - state(테스트 상태)
    - testRunDurationMs는 테스트 실행 시간(ms 단위) 입니다.
    - testRunDurationMs가 길수록 전체 처리 시간이 오래 걸렸다고 판단할 수 있습니다.
  - http_req_duration(응답 시간 통계)
    - 전반적인 응답 시간이 얼마나 걸렸는지 통계치로 확인할 수 있습니다.
    - p(90)은 상위 10%의 요청 응답 시간, p(95)은 상위 5%의 요청 응답시간을 의미합니다.
- k6 테스트 결과 summary 판별 기준
  - http_req_failed(http 요청 실패율)
    - rate가 0이어야 합니다.
    - 실패요청이 존재한다면 원인을 찾고 해결하여 테스트 시나리오를 조정하고 다시 진행합니다.
  - http_reqs (총 요청 수 및 초당 요청 수)
    - 시나리오에서 설정한 수와 동일한 수가 count에 표시되어야 합니다.
  - state(테스트 상태)
    - testRunDurationMs가 짧을수록 성능이 좋습니다.
  - http_req_duration(응답 시간 통계)
    - 대부분의 지표가 더 낮을수록 좋지만  
      여러 상충되는 정보가 존재한다면 avg가 더 낮은 시나리오가 더 좋다고 판단합니다.

  
