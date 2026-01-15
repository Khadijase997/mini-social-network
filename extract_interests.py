import csv
import re

interests = set()
with open('src/main/resources/data/SocialMediaUsersDataset.csv', mode='r', encoding='utf-8') as f:
    reader = csv.DictReader(f)
    for row in reader:
        # Interests are formatted like "'Interest1', 'Interest2'"
        raw_interests = row['Interests']
        parts = re.findall(r"'(.*?)'", raw_interests)
        for p in parts:
            interests.add(p.strip())

sorted_interests = sorted(list(interests))
print("Total unique interests:", len(sorted_interests))
for i in sorted_interests:
    print(f'"{i}",')
