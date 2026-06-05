import urllib.request
import zipfile
import pathlib
import shutil

url = 'https://dl.google.com/android/repository/commandlinetools-win-9477386_latest.zip'
zip_path = pathlib.Path('commandlinetools-win.zip')
root = pathlib.Path('android-sdk')

if not zip_path.exists():
    print('Downloading command line tools...')
    urllib.request.urlretrieve(url, zip_path)

if root.exists():
    print('Removing existing android-sdk folder...')
    shutil.rmtree(root)

print('Extracting command line tools...')
with zipfile.ZipFile(zip_path, 'r') as z:
    z.extractall(root)

print('Extracted to:', root)
print('Contents:', [p.name for p in root.iterdir()])
