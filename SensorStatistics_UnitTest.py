import unittest
from your_module_name import SensorStats, parse_line, process_file, main

class TestSensorStatistics(unittest.TestCase):

    def test_parse_line(self):
        # Test parsing valid and invalid data
        self.assertEqual(parse_line("s1,10"), ("s1", 10))
        self.assertEqual(parse_line("s2,NaN"), ("s2", None))
        self.assertEqual(parse_line("invalid,line"), None)

    def test_process_file(self):
        # Test processing multiple lines
        content = """sensor-id,humidity
s1,10
s2,88
s1,NaN"""
        expected = [("s1", 10), ("s2", 88), ("s1", None)]
        self.assertEqual(process_file(content), expected)

    def test_sensor_stats_initialization(self):
        # Test the initialization of SensorStats
        stats = SensorStats("s1")
        self.assertEqual(stats.sensor_id, "s1")
        self.assertIsNone(stats.min)
        self.assertIsNone(stats.max)
        self.assertEqual(stats.sum, 0)
        self.assertEqual(stats.count, 0)
        self.assertEqual(stats.failed_count, 0)

    def test_sensor_stats_merge(self):
        # Test merging data into stats
        stats = SensorStats("s1")
        stats.merge(10)
        stats.merge(20)
        stats.merge(None)
        self.assertEqual(stats.min, 10)
        self.assertEqual(stats.max, 20)
        self.assertEqual(stats.sum, 30)
        self.assertEqual(stats.count, 2)
        self.assertEqual(stats.failed_count, 1)
        self.assertAlmostEqual(stats.avg(), 15.0)

    def test_sorted_output(self):
        # Redirect stdout and test the output
        import io
        import sys
        captured_output = io.StringIO()  # Create StringIO object
        sys.stdout = captured_output  # Redirect stdout.
        main()  # Call function.
        sys.stdout = sys.__stdout__  # Reset redirect.
        self.assertIn("s2,78,82.00,88", captured_output.getvalue())  # Check if expected string is in output

# Run the tests
if __name__ == '__main__':
    unittest.main()
