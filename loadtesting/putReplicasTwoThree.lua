-- example dynamic request script which demonstrates changing
-- the request path and a header for each request
-------------------------------------------------------------
-- NOTE: each wrk thread has an independent Lua scripting
-- context and thus there will be one counter per thread

counter = 0
wrk.method = "PUT"

request = function()
    path = "/v0/entity?id=" .. counter .. "&replicas=2/3"
    wrk.body = "/v0/entity?id=1&replicas=2/3"
    counter = counter + 1
    return wrk.format(nil, path)
end