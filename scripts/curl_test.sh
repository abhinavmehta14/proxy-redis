#/bin/bash
for i in {0..100}
do
  echo -e "$i \c"
  curl -i -XGET http://localhost:8080/v1.0/proxy/?key=a1
  curl -i -XGET http://localhost:8080/v1.0/proxy/?key=a1
  curl -i -XGET http://localhost:8080/v1.0/proxy/?key=does_not_exist
  echo
done
