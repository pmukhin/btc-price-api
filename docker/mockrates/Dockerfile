FROM busybox

ADD rates.json /www/rates.json

EXPOSE 8086

# Create a basic webserver and sleep forever
CMD httpd -p 8086 -h /www; tail -f /dev/null