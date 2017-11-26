# Report

Находимся в корне проекта:

```
$ pwd
/home/olerom/Projects/2017-highload-kv
```

## Результаты нагрузочного тестирования до оптимизаций


`PUT` без перезаписи с `replicas=2/3`: 

```
$ wrk --latency -c4 -d5m -s loadtesting/scripts/putReplicasTwoThree.lua http://localhost:8080
Running 5m test @ http://localhost:8080
  2 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    10.87ms   43.78ms 705.30ms   95.40%
    Req/Sec   454.79    106.18   750.00     85.15%
  Latency Distribution
     50%    2.05ms
     75%    2.55ms
     90%    4.90ms
     99%  241.40ms
  264348 requests in 5.00m, 18.66MB read
Requests/sec:    880.74
Transfer/sec:     63.65KB
```

`PUT` без перезаписи с `replicas=3/3`: 

```
$ wrk --latency -c4 -d5m -s loadtesting/scripts/putReplicasThreeThree.lua http://localhost:8080
Running 5m test @ http://localhost:8080
  2 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    12.86ms   52.28ms 806.17ms   95.11%
    Req/Sec   450.84    114.37   747.00     84.88%
  Latency Distribution
     50%    2.05ms
     75%    2.56ms
     90%    6.06ms
     99%  269.21ms
  261081 requests in 5.00m, 18.42MB read
Requests/sec:    870.00
Transfer/sec:     62.87KB
```

На имеющихся данных `GET` без повторов с `replicas=2/3`:

```
$ wrk --latency -c4 -d5m -s loadtesting/scripts/getReplicasTwoThree.lua http://localhost:8080
Running 5m test @ http://localhost:8080
  2 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    15.46ms   51.12ms   1.12s    95.26%
    Req/Sec   505.12    319.90     1.76k    69.19%
  Latency Distribution
     50%    2.09ms
     75%    9.93ms
     90%   27.71ms
     99%  249.41ms
  292549 requests in 5.00m, 277.59MB read
Requests/sec:    974.90
Transfer/sec:      0.93MB
```

На имеющихся данных `GET` без повторов с `replicas=3/3`:

```
$ wrk --latency -c4 -d5m -s loadtesting/scripts/getReplicasThreeThree.lua http://localhost:8080
Running 5m test @ http://localhost:8080
  2 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    13.29ms   38.22ms 668.96ms   93.79%
    Req/Sec   615.51    443.30     2.12k    70.87%
  Latency Distribution
     50%    1.76ms
     75%    8.85ms
     90%   28.89ms
     99%  184.65ms
  356669 requests in 5.00m, 283.55MB read
  Non-2xx or 3xx responses: 90908
Requests/sec:   1188.56
Transfer/sec:      0.94MB
```

```
$ wrk --latency -c4 -d5m -s loadtesting/scripts/putReplicasRepeatTwoThree.lua http://localhost:8080
Running 5m test @ http://localhost:8080
  2 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     3.24ms   13.95ms 543.62ms   98.79%
    Req/Sec   496.99     62.59   620.00     86.67%
  Latency Distribution
     50%    2.00ms
     75%    2.31ms
     90%    2.86ms
     99%   26.23ms
  296221 requests in 5.00m, 20.90MB read
Requests/sec:    987.23
Transfer/sec:     71.34KB
```

``` 
$ wrk --latency -c4 -d5m -s loadtesting/scripts/putReplicasRepeatThreeThree.lua http://localhost:8080
Running 5m test @ http://localhost:8080
  2 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     3.06ms   12.66ms 518.17ms   98.92%
    Req/Sec   496.48     58.34   610.00     85.12%
  Latency Distribution
     50%    2.00ms
     75%    2.31ms
     90%    2.88ms
     99%   18.11ms
  296018 requests in 5.00m, 20.89MB read
Requests/sec:    986.52
Transfer/sec:     71.29KB
```

``` 
$ wrk --latency -c4 -d5m -s loadtesting/scripts/getReplicasRepeatTwoThree.lua http://localhost:8080
Running 5m test @ http://localhost:8080
  2 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     1.40ms    1.83ms 112.38ms   97.63%
    Req/Sec     1.52k   103.86     1.74k    86.33%
  Latency Distribution
     50%    1.10ms
     75%    1.63ms
     90%    2.08ms
     99%    4.84ms
  909284 requests in 5.00m, 0.92GB read
Requests/sec:   3030.42
Transfer/sec:      3.15MB
```