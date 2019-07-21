#/bin/bash
for i in {0..5}
do
  echo "starting curl test #$i"
  ./scripts/curl_test.sh &
done
