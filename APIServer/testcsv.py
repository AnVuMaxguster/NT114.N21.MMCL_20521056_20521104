import pandas as pd

# Read the CSV file
df = pd.read_csv('auto-mpg.csv')

# Extract unique values from a specific column into an array
column_values = df['acceleration'].unique()

# Iterate through the array and print out the unique values
for value in column_values:
    print(value)