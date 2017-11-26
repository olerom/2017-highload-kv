# Report

Находимся в корне проекта:

```
$ pwd
/home/olerom/Projects/2017-highload-kv
```

## Результаты нагрузочного тестирования до оптимизаций


### `PUT` без перезаписи с `replicas=2/3`
1. 2 threads and 4 connections:

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

2. 4 threads and 4 connections:
```
$ wrk --latency -t4 -c4 -d5m -s loadtesting/scripts/putReplicasTwoThree.lua http://localhost:8080
Running 5m test @ http://localhost:8080
  4 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     4.79ms   17.95ms 489.39ms   96.19%
    Req/Sec   234.49     47.37   430.00     84.66%
  Latency Distribution
     50%    1.52ms
     75%    1.96ms
     90%    3.38ms
     99%   91.08ms
  278510 requests in 5.00m, 19.65MB read
Requests/sec:    928.06
Transfer/sec:     67.07KB
```

### `PUT` без перезаписи с `replicas=3/3`: 

1. 2 threads and 4 connections
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

2. 4 threads and 4 connections
```
$ wrk --latency -t4 -c4 -d5m -s loadtesting/scripts/putReplicasThreeThree.lua http://localhost:8080
Running 5m test @ http://localhost:8080
  4 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     4.25ms   16.60ms 383.74ms   97.25%
    Req/Sec   230.91     45.02   343.00     80.94%
  Latency Distribution
     50%    1.67ms
     75%    2.30ms
     90%    3.59ms
     99%   80.71ms
  274376 requests in 5.00m, 19.36MB read
Requests/sec:    914.36
Transfer/sec:     66.08KB
```

### На имеющихся данных `GET` без повторов с `replicas=2/3`:
1. 2 threads and 4 connections
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

2. 4 threads and 4 connections

```
$ wrk --latency -t4 -c4 -d5m -s loadtesting/scripts/getReplicasTwoThree.lua http://localhost:8080
Running 5m test @ http://localhost:8080
  4 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     5.70ms   17.60ms 448.51ms   97.10%
    Req/Sec   401.70    240.70     1.06k    75.59%
  Latency Distribution
     50%    1.46ms
     75%    5.49ms
     90%   11.30ms
     99%   66.02ms
  476399 requests in 5.00m, 318.57MB read
Requests/sec:   1587.52
Transfer/sec:      1.06MB
```


### На имеющихся данных `GET` без повторов с `replicas=3/3`:

1. 2 threads and 4 connections
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

2. 4 threads and 4 connections
```
$ wrk --latency -t4 -c4 -d5m -s loadtesting/scripts/getReplicasThreeThree.lua http://localhost:8080
Running 5m test @ http://localhost:8080
  4 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     4.37ms   15.47ms 448.20ms   97.75%
    Req/Sec   503.28    267.24     1.27k    74.61%
  Latency Distribution
     50%    1.40ms
     75%    3.29ms
     90%    8.83ms
     99%   44.06ms
  597919 requests in 5.00m, 332.94MB read
  Non-2xx or 3xx responses: 301420
Requests/sec:   1992.41
Transfer/sec:      1.11MB
```


### `PUT` c перезаписью с `replicas=2/3`
1. 2 threads and 4 connections
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

2. 4 threads and 4 connections
```
$ wrk --latency -t4 -c4 -d5m -s loadtesting/scripts/putReplicasRepeatTwoThree.lua http://localhost:8080
Running 5m test @ http://localhost:8080
  4 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     3.08ms   23.70ms 955.67ms   99.09%
    Req/Sec   251.32     30.57   353.00     81.79%
  Latency Distribution
     50%    1.46ms
     75%    1.88ms
     90%    2.39ms
     99%   23.53ms
  299628 requests in 5.00m, 21.15MB read
Requests/sec:    998.51
Transfer/sec:     72.16KB
```

### `PUT` c перезаписью с `replicas=3/3`
1. 2 threads and 4 connections
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

2. 4 threads and 4 connections
```
$ wrk --latency -t4 -c4 -d5m -s loadtesting/scripts/putReplicasRepeatThreeThree.lua http://localhost:8080
Running 5m test @ http://localhost:8080
  4 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     3.81ms   13.15ms 410.03ms   96.66%
    Req/Sec   223.46     50.88   460.00     72.15%
  Latency Distribution
     50%    1.63ms
     75%    2.36ms
     90%    4.17ms
     99%   52.27ms
  266271 requests in 5.00m, 18.79MB read
Requests/sec:    887.33
Transfer/sec:     64.12KB
```

### `GET` c перезаписью с `replicas=2/3`

1. 2 threads and 4 connections
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

2. 4 threads and 4 connections
```
$ wrk --latency -t4 -c4 -d5m -s loadtesting/scripts/getReplicasRepeatTwoThree.lua http://localhost:8080
Running 5m test @ http://localhost:8080
  4 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     1.34ms  823.23us  31.52ms   91.56%
    Req/Sec   766.84    233.98     1.15k    56.78%
  Latency Distribution
     50%    1.09ms
     75%    1.62ms
     90%    2.07ms
     99%    4.42ms
  916021 requests in 5.00m, 0.93GB read
Requests/sec:   3052.66
Transfer/sec:      3.17MB
```


### `GET` c перезаписью с `replicas=3/3`
1. 2 threads and 4 connections
```
$ wrk --latency -c4 -d5m -s loadtesting/scripts/getReplicasRepeatThreeThree.lua http://localhost:8080
Running 5m test @ http://localhost:8080
  2 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     1.35ms  747.18us  28.16ms   90.15%
    Req/Sec     1.51k    73.12     1.74k    73.63%
  Latency Distribution
     50%    1.12ms
     75%    1.65ms
     90%    2.08ms
     99%    4.30ms
  899075 requests in 5.00m, 0.91GB read
Requests/sec:   2996.33
Transfer/sec:      3.11MB
```

2. 4 threads and 4 connections
```
$ wrk --latency -t4 -c4 -d5m -s loadtesting/scripts/getReplicasRepeatThreeThree.lua http://localhost:8080
Running 5m test @ http://localhost:8080
  4 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     1.35ms    1.28ms  74.71ms   96.94%
    Req/Sec   784.92     73.07     0.97k    85.58%
  Latency Distribution
     50%    1.16ms
     75%    1.42ms
     90%    1.77ms
     99%    5.36ms
  937587 requests in 5.00m, 0.95GB read
Requests/sec:   3124.72
Transfer/sec:      3.25MB
```


## Результаты нагрузочного тестирования после оптимизаций


### `PUT` без перезаписи с `replicas=2/3`
1. 2 threads and 4 connections:

```
$ wrk --latency -c4 -d5m -s loadtesting/scripts/putReplicasTwoThree.lua http://localhost:8080
Running 5m test @ http://localhost:8080
  2 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     5.12ms   28.01ms 589.50ms   98.00%
    Req/Sec   562.89     88.01   750.00     86.68%
  Latency Distribution
     50%    1.70ms
     75%    1.87ms
     90%    2.24ms
     99%  124.80ms
  332721 requests in 5.00m, 23.48MB read
Requests/sec:   1108.72
Transfer/sec:     80.12KB
```

2. 4 threads and 4 connections:
```
$ wrk --latency -c4 -d5m -s loadtesting/scripts/putReplicasTwoThree.lua http://localhost:8080
Running 5m test @ http://localhost:8080
  2 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     5.12ms   28.01ms 589.50ms   98.00%
    Req/Sec   562.89     88.01   750.00     86.68%
  Latency Distribution
     50%    1.70ms
     75%    1.87ms
     90%    2.24ms
     99%  124.80ms
  332721 requests in 5.00m, 23.48MB read
Requests/sec:   1108.72
Transfer/sec:     80.12KB
olerom@olerom-desktop:~/Projects/2017-highload-kv$ wrk --latency -t4 -c4 -d5m -s loadtesting/scripts/putReplicasTwoThree.lua http://localhost:8080
Running 5m test @ http://localhost:8080
  4 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     3.72ms   30.49ms 636.27ms   98.51%
    Req/Sec   305.89     53.22   530.00     76.84%
  Latency Distribution
     50%  537.00us
     75%  777.00us
     90%    1.35ms
     99%   64.31ms
  362929 requests in 5.00m, 25.61MB read
Requests/sec:   1209.41
Transfer/sec:     87.40KB
```

### `PUT` без перезаписи с `replicas=3/3`: 

1. 2 threads and 4 connections
```
$ wrk --latency -c4 -d5m -s loadtesting/scripts/putReplicasThreeThree.lua http://localhost:8080
Running 5m test @ http://localhost:8080
  2 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     4.91ms   26.58ms 507.31ms   98.06%
    Req/Sec   565.04     83.04   730.00     86.06%
  Latency Distribution
     50%    1.70ms
     75%    1.87ms
     90%    2.22ms
     99%  123.57ms
  334287 requests in 5.00m, 23.59MB read
Requests/sec:   1114.15
Transfer/sec:     80.51KB
```

2. 4 threads and 4 connections
```
$ wrk --latency -t4 -c4 -d5m -s loadtesting/scripts/putReplicasThreeThree.lua http://localhost:8080
Running 5m test @ http://localhost:8080
  4 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     1.78ms   12.00ms 362.87ms   98.66%
    Req/Sec   309.30     48.66   580.00     77.31%
  Latency Distribution
     50%  548.00us
     75%  786.00us
     90%    1.27ms
     99%   29.35ms
  368172 requests in 5.00m, 25.98MB read
Requests/sec:   1226.97
Transfer/sec:     88.67KB
```

### На имеющихся данных `GET` без повторов с `replicas=2/3`:
1. 2 threads and 4 connections
```
$ wrk --latency -c4 -d5m -s loadtesting/scripts/getReplicasTwoThree.lua http://localhost:8080
Running 5m test @ http://localhost:8080
  2 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   813.30us    9.49ms 610.13ms   99.70%
    Req/Sec     4.12k   578.44     6.27k    84.94%
  Latency Distribution
     50%  422.00us
     75%  499.00us
     90%  595.00us
     99%    2.60ms
  2457501 requests in 5.00m, 330.10MB read
Requests/sec:   8189.82
Transfer/sec:      1.10MB
```

2. 4 threads and 4 connections

```
$ wrk --latency -t4 -c4 -d5m -s loadtesting/scripts/getReplicasTwoThree.lua http://localhost:8080
Running 5m test @ http://localhost:8080
  4 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     0.95ms   12.91ms 725.94ms   99.78%
    Req/Sec     2.07k   422.11     3.66k    67.87%
  Latency Distribution
     50%  415.00us
     75%  526.00us
     90%  657.00us
     99%    2.74ms
  2471470 requests in 5.00m, 515.73MB read
Requests/sec:   8237.40
Transfer/sec:      1.72MB
```


### На имеющихся данных `GET` без повторов с `replicas=3/3`:

1. 2 threads and 4 connections
```
$ wrk --latency -c4 -d5m -s loadtesting/scripts/getReplicasThreeThree.lua http://localhost:8080
Running 5m test @ http://localhost:8080
  2 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   545.34us    1.69ms 176.84ms   98.74%
    Req/Sec     4.00k   453.51     5.51k    90.88%
  Latency Distribution
     50%  435.00us
     75%  530.00us
     90%  626.00us
     99%    2.76ms
  2385847 requests in 5.00m, 378.25MB read
  Non-2xx or 3xx responses: 2197168
Requests/sec:   7951.93
Transfer/sec:      1.26MB
```

2. 4 threads and 4 connections
```
$ wrk --latency -t4 -c4 -d5m -s loadtesting/scripts/getReplicasThreeThree.lua http://localhost:8080
Running 5m test @ http://localhost:8080
  4 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   487.48us  370.41us  22.13ms   95.59%
    Req/Sec     2.05k   435.36     3.18k    59.57%
  Latency Distribution
     50%  417.00us
     75%  539.00us
     90%  654.00us
     99%    1.89ms
  2453034 requests in 5.00m, 564.12MB read
  Non-2xx or 3xx responses: 2075675
Requests/sec:   8175.98
Transfer/sec:      1.88MB
```


### `PUT` c перезаписью с `replicas=2/3`
1. 2 threads and 4 connections
```
$ wrk --latency -c4 -d5m -s loadtesting/scripts/putReplicasRepeatTwoThree.lua http://localhost:8080
Running 5m test @ http://localhost:8080
  2 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     1.89ms    2.08ms  99.38ms   98.11%
    Req/Sec   582.49     61.29   707.00     82.95%
  Latency Distribution
     50%    1.67ms
     75%    1.80ms
     90%    2.07ms
     99%    6.69ms
  347842 requests in 5.00m, 24.55MB read
Requests/sec:   1159.15
Transfer/sec:     83.77KB
```

2. 4 threads and 4 connections
```
$ wrk --latency -t4 -c4 -d5m -s loadtesting/scripts/putReplicasRepeatTwoThree.lua http://localhost:8080
Running 5m test @ http://localhost:8080
  4 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   727.31us    1.64ms  86.85ms   98.10%
    Req/Sec   320.38     43.78   620.00     72.77%
  Latency Distribution
     50%  497.00us
     75%  690.00us
     90%    1.04ms
     99%    5.05ms
  382922 requests in 5.00m, 27.02MB read
Requests/sec:   1276.03
Transfer/sec:     92.21KB
```

### `PUT` c перезаписью с `replicas=3/3`
1. 2 threads and 4 connections
``` 
$ wrk --latency -c4 -d5m -s loadtesting/scripts/putReplicasRepeatThreeThree.lua http://localhost:8080
Running 5m test @ http://localhost:8080
  2 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     1.81ms    1.24ms  68.53ms   97.88%
    Req/Sec   592.41     60.50   770.00     72.34%
  Latency Distribution
     50%    1.66ms
     75%    1.81ms
     90%    2.04ms
     99%    4.97ms
  353879 requests in 5.00m, 24.97MB read
Requests/sec:   1179.20
Transfer/sec:     85.22KB
```

2. 4 threads and 4 connections
```
$ wrk --latency -t4 -c4 -d5m -s loadtesting/scripts/putReplicasRepeatThreeThree.lua http://localhost:8080
Running 5m test @ http://localhost:8080
  4 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   792.26us    2.45ms 113.98ms   98.24%
    Req/Sec   320.27     45.57   560.00     70.03%
  Latency Distribution
     50%  494.00us
     75%  693.00us
     90%    1.05ms
     99%    6.43ms
  382772 requests in 5.00m, 27.01MB read
Requests/sec:   1275.52
Transfer/sec:     92.18KB
```

### `GET` c перезаписью с `replicas=2/3`

1. 2 threads and 4 connections
```
$ wrk --latency -c4 -d5m -s loadtesting/scripts/getReplicasRepeatTwoThree.lua http://localhost:8080
Running 5m test @ http://localhost:8080
  2 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   342.33us  350.31us  27.55ms   97.89%
    Req/Sec     5.78k   245.57     6.12k    92.40%
  Latency Distribution
     50%  297.00us
     75%  344.00us
     90%  406.00us
     99%    1.49ms
  3451295 requests in 5.00m, 3.50GB read
Requests/sec:  11503.72
Transfer/sec:     11.95MB
```

2. 4 threads and 4 connections
```
$ wrk --latency -t4 -c4 -d5m -s loadtesting/scripts/getReplicasRepeatTwoThree.lua http://localhost:8080
Running 5m test @ http://localhost:8080
  4 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   378.60us  535.70us  37.92ms   97.76%
    Req/Sec     2.77k   204.74     3.11k    93.29%
  Latency Distribution
     50%  313.00us
     75%  373.00us
     90%  455.00us
     99%    1.75ms
  3310072 requests in 5.00m, 3.36GB read
Requests/sec:  11031.89
Transfer/sec:     11.46MB
```


### `GET` c перезаписью с `replicas=3/3`
1. 2 threads and 4 connections
```
$ wrk --latency -c4 -d5m -s loadtesting/scripts/getReplicasRepeatThreeThree.lua http://localhost:8080
Running 5m test @ http://localhost:8080
  2 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   361.41us  283.93us  22.55ms   97.38%
    Req/Sec     5.37k   222.89     5.68k    93.48%
  Latency Distribution
     50%  323.00us
     75%  389.00us
     90%  459.00us
     99%    1.39ms
  3204371 requests in 5.00m, 3.25GB read
Requests/sec:  10680.89
Transfer/sec:     11.09MB
```

2. 4 threads and 4 connections
```
$ wrk --latency -t4 -c4 -d5m -s loadtesting/scripts/getReplicasRepeatThreeThree.lua http://localhost:8080
Running 5m test @ http://localhost:8080
  4 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   405.36us  308.97us  24.30ms   96.80%
    Req/Sec     2.44k   139.41     2.73k    85.53%
  Latency Distribution
     50%  362.00us
     75%  404.00us
     90%  480.00us
     99%    1.47ms
  2910511 requests in 5.00m, 2.95GB read
Requests/sec:   9700.86
Transfer/sec:     10.07MB
```

## Conclusion

- Результаты для `GET` существенно улучшились, так как всегда использовались горячие кэши.

- Результаты для `PUT` улучшились. 
Если верить `jvisualvm`, то для улучшения надо смотреть сторону `HttpClient` (создание объектов `ResponseReader`).
