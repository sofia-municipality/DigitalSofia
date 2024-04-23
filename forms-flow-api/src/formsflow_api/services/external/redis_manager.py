import redis

class RedisManager:
    def __init__(self, connection_string):
        # Parse the connection string
        self.redis = redis.Redis.from_url(connection_string)
    
    def ExistKey(self, key: str) -> bool:
        # Check if a key exists in Redis
        return self.redis.exists(key) > 0
    
    def CreateKey(self, key: str, value: str, TTL: int = None):
        # Set a key with value; if TTL is provided, set it with expiration, otherwise without expiration
        if TTL is not None:
            self.redis.setex(key, TTL, value)
        else:
            self.redis.set(key, value)
    
    def DeleteKey(self, key: str) -> bool:
        # Delete a key
        return self.redis.delete(key) > 0
    
    def GetKeyValue(self, key: str):
        # Get the value of a key, if not found return None
        result = self.redis.get(key)
        return result.decode('utf-8') if result else None
    
    def Ping(self) -> bool:
        # Check if Redis server is alive
        return self.redis.ping()
    
    @staticmethod
    def BuildKeyName(*args) -> str:
        # Escape existing colons in arguments and concatenate with a colon as the delimiter to create a Redis key
        escaped_args = [arg.replace(":", "|") for arg in args]
        return ':'.join(escaped_args)