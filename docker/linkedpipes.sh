echo '############################'
echo 'Updating pipeline configuration'
echo '############################'
bash ./updatePipelinesConfig.sh

echo '############################'
echo 'Starting executor service...'
echo '############################'

bash ./executor.sh & # >> ./data/logs/executor.log &

echo '####################################'
echo 'Starting executor-monitor service...'
echo '####################################'

bash ./executor-monitor.sh & # >> ./data/logs/executor-monitor.log &

echo '###########################'
echo 'Starting storage service...'
echo '###########################'

bash ./storage.sh & # >> ./data/logs/storage.log &

echo '##############################################################################'
echo 'Starting frontend service... (check Docker container port mappings for access)'
echo '##############################################################################'

bash ./frontend.sh # >> ./data/logs/frontend/frontend.log

echo 'LinkedPipes running succesfully!'
