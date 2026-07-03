import zipfile
z = zipfile.ZipFile('/home/ubuntu/weddingcard-0.0.1-SNAPSHOT.jar')
content = z.read('BOOT-INF/classes/templates/view.html').decode()
idx = content.find('rsvp-pill.active')
print(content[idx:idx+120])
