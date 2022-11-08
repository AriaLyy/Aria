# -*- coding: utf-8 -*-
import os
from flask import Flask, request, url_for, send_from_directory
from werkzeug import secure_filename

ALLOWED_EXTENSIONS = set(['png', 'jpg', 'jpeg', 'gif', 'rar', 'apk', 'zip'])

app = Flask(__name__)
app.config['UPLOAD_FOLDER'] = '/Users/aria/temp/test/'
app.config['MAX_CONTENT_LENGTH'] = 1600 * 1024 * 1024

"""
可以选择这个扩展
Flask-Uploads
"""

html = '''
    <!DOCTYPE html>
    <title>Upload File</title>
    <h1>图片上传</h1>
    <form method=post enctype=multipart/form-data>
         <input type=file name=file>
         <input type=submit value=上传>
    </form>
    '''


def allowed_file(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1] in ALLOWED_EXTENSIONS


@app.route('/uploads/<filename>')
def uploaded_file(filename):
    return send_from_directory(app.config['UPLOAD_FOLDER'],
                               filename)


@app.route('/', methods=['GET', 'POST'])
def test():
    return'test'


@app.route('/upload/', methods=['GET', 'POST'])
def upload_file():
    if request.method == 'POST':
        print(request.values)
        print('params = ' + request.values.get('params'))

        file = request.files['file']
        print(file)
        #if file and allowed_file(file.filename):
        print('start save')
        filename = secure_filename(file.filename)
        file.save(os.path.join(app.config['UPLOAD_FOLDER'], filename))
            # file_url = url_for('uploaded_file', filename=filename)
            # return html + '<br><img src=' + file_url + '>'
        return '200'
    return '405'


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
