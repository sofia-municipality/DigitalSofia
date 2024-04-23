# import unittest

# import sys
# import os

# # Debugging step: Print the current directory and the target path
# print("Current Script Directory:", os.path.dirname(__file__))

# current_dir = os.path.dirname(__file__)
# relative_path = '../../../../../src/formsflow_api/services/external'
# redis_manager_path = os.path.abspath(os.path.join(current_dir, relative_path))

# print("Attempting to add to sys.path:", redis_manager_path)  # Debugging line

# sys.path.append(redis_manager_path)

# # Attempt the import again
# try:
#     from redis_manager import RedisManager
#     print("Import succeeded.")
# except ModuleNotFoundError as e:
#     print(f"Import failed: {e}")



# class TestRedisManager(unittest.TestCase):
#     @classmethod
#     def setUpClass(cls):
#         # Initialize RedisManager with a connection string to your Redis server
#         cls.redis_manager = RedisManager("redis://localhost:6379/0")
        
#         # A test key-value pair for use in tests
#         cls.test_key = "test:key"
#         cls.test_value = "value"
    
#     def test_ping(self):
#         self.assertTrue(self.redis_manager.Ping())
    
#     def test_create_and_get_key_value(self):
#         self.redis_manager.CreateKey(self.test_key, self.test_value)
#         value = self.redis_manager.GetKeyValue(self.test_key)
#         self.assertEqual(value, self.test_value)
    
#     def test_key_existence(self):
#         self.redis_manager.CreateKey(self.test_key, self.test_value)
#         exists = self.redis_manager.ExistKey(self.test_key)
#         self.assertTrue(exists)
    
#     def test_delete_key(self):
#         self.redis_manager.CreateKey(self.test_key, self.test_value)
#         deleted = self.redis_manager.DeleteKey(self.test_key)
#         self.assertTrue(deleted)
#         exists_after_delete = self.redis_manager.ExistKey(self.test_key)
#         self.assertFalse(exists_after_delete)
    
#     def test_get_key_value_non_existent(self):
#         non_existent_key = "nonexistent:key"
#         value = self.redis_manager.GetKeyValue(non_existent_key)
#         self.assertIsNone(value)
    
#     def test_build_key_name(self):
#         key_name = RedisManager.BuildKeyName("part1", "part2:with_colon", "part3")
#         self.assertEqual(key_name, "part1:part2|with_colon:part3")

# if __name__ == "__main__":
#     unittest.main()
