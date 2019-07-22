#/bin/bash
# TODO: assumes curl command which is not part of alpine. Find alternatives or a custom alpine image
# TODO: add keys a1, a2, a3 to redis
# TODO: assert response codes from each of the curl requests below
curl_code="curl -s -o /dev/null -XGET -w %{http_code}\t"
for i in {0..100}
do
  echo -e "$i \c"
  ${curl_code} http://localhost:8080/v1.0/proxy/?key=a1
  ${curl_code} http://localhost:8080/v1.0/proxy/?key=a2
  ${curl_code} http://localhost:8080/v1.0/proxy/?key=a3
  ${curl_code} http://localhost:8080/v1.0/proxy/?key=does_not_exist
  echo
done
