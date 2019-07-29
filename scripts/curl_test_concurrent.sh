#/bin/bash
for i in {0..320}
do
  echo "starting curl test #$i"
  ./scripts/curl_test.sh &
done
