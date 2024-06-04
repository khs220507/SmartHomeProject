from flask import Flask, jsonify
from flask_sqlalchemy import SQLAlchemy
from pythermalcomfort.models import pmv
from pythermalcomfort.utilities import v_relative, clo_dynamic
import requests
from datetime import datetime

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = 'mysql://root:220507@localhost/sensor'
db = SQLAlchemy(app)


class SensorData(db.Model):
    __tablename__ = 'sensor_data'
    id = db.Column(db.Integer, primary_key=True)
    temperature = db.Column(db.Float)
    humidity = db.Column(db.Float)
    timestamp = db.Column(db.DateTime)

class PMVData(db.Model):
    __tablename__ = 'pmv_data'
    id = db.Column(db.Integer, primary_key=True)
    pmv = db.Column(db.Float)
    timestamp = db.Column(db.DateTime)


@app.route('/latest10PMVData', methods=['GET'])
def get_latest_10_pmv_data():
    latest_10_sensor_data = SensorData.query.order_by(SensorData.timestamp.desc()).limit(10).all()
    pmv_data_list = []

    for data in latest_10_sensor_data:
        t_db = data.temperature
        t_r = data.temperature
        relative_humidity = data.humidity
        v = 0.1
        met_rate = 1.
        clo_insulation = 0.5
        v_r = v_relative(v=v, met=met_rate)
        clo_d = clo_dynamic(clo=clo_insulation, met=met_rate)
        pmv_value = pmv(tdb=t_db, tr=t_r, vr=v_r, rh=relative_humidity, met=met_rate, clo=clo_d)
        pmv_data_list.append({'temperature': data.temperature, 'humidity': data.humidity, 'pmv': pmv_value})

        pmv_data = PMVData(pmv=pmv_value, timestamp=datetime.now())
        db.session.add(pmv_data)

    db.session.commit()  # 데이터베이스에 커밋

    spring_boot_app_url = "http://172.30.1.2:8080/latest10PMVData"
    response = requests.post(spring_boot_app_url, json=pmv_data_list)

    return jsonify(pmv_data_list), 200







if __name__ == '__main__':
    with app.app_context():
        db.create_all()  # 모든 테이블을 데이터베이스에 생성
    app.run(debug=True, host='0.0.0.0')
