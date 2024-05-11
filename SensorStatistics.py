class SensorStats:
    def __init__(self, sensor_id):
        self.sensor_id = sensor_id
        self.min = None
        self.max = None
        self.sum = 0
        self.count = 0
        self.failed_count = 0

    def avg(self):
        return self.sum / self.count if self.count > 0 else None

    def merge(self, humidity):
        if humidity is not None:
            if self.min is None or humidity < self.min:
                self.min = humidity
            if self.max is None or humidity > self.max:
                self.max = humidity
            self.sum += humidity
            self.count += 1
        else:
            self.failed_count += 1

def parse_line(line):
    parts = line.strip().split(',')
    if len(parts) == 2:
        sensor_id, humidity = parts
        try:
            humidity = int(humidity)
        except ValueError:
            humidity = None
        return sensor_id, humidity
    return None

def process_file(content):
    lines = content.strip().split('\n')[1:]  # Skip header
    return [parse_line(line) for line in lines]

def main():
    csv1 = """sensor-id,humidity
s1,10
s2,88
s1,NaN"""
    csv2 = """sensor-id,humidity
s2,80
s3,NaN
s2,78
s1,98"""

    files_content = [csv1, csv2]
    stats_map = {}

    # Process each file content
    for content in files_content:
        measurements = process_file(content)
        for measurement in measurements:
            if measurement:
                sensor_id, humidity = measurement
                if sensor_id not in stats_map:
                    stats_map[sensor_id] = SensorStats(sensor_id)
                stats_map[sensor_id].merge(humidity)

    total_measurements = sum(len(process_file(content)) for content in files_content)
    total_failures = sum(stats_map[sensor].failed_count for sensor in stats_map)

    print(f"Num of processed files: {len(files_content)}")
    print(f"Num of processed measurements: {total_measurements}")
    print(f"Num of failed measurements: {total_failures}")
    print("\nSensors with highest avg humidity:")

    # Sorting sensors by highest avg humidity
    sorted_sensors = sorted(stats_map.values(), key=lambda x: (-x.avg() if x.avg() is not None else float('-inf'), x.sensor_id))
    
    print("sensor-id,min,avg,max")
    for sensor in sorted_sensors:
        min_val = sensor.min if sensor.min is not None else 'NaN'
        avg_val = f"{sensor.avg():.2f}" if sensor.avg() is not None else 'NaN'
        max_val = sensor.max if sensor.max is not None else 'NaN'
        print(f"{sensor.sensor_id},{min_val},{avg_val},{max_val}")

if __name__ == "__main__":
    main()
