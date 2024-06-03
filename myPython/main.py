from flask import Flask, jsonify
from flask_sqlalchemy import SQLAlchemy
from pythermalcomfort.models import pmv
from pythermalcomfort.utilities import v_relative, clo_dynamic
import requests


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


@app.route('/latestPMVData', methods=['GET'])
def calculate_pmv():
    latest_data = SensorData.query.order_by(SensorData.timestamp.desc()).first()
    # DB에서 데이터를 불러왔는지 확인
    if latest_data is not None:
        # 실내온도 tdb 및 평균 방사온도 tr을 최신 데이터로 설정하여 PMV를 계산

        t_db = latest_data.temperature;
        t_r = latest_data.temperature;
        relative_humidity = latest_data.humidity;
        v = 0.1
        met_rate = 1.
        clo_insulation = 0.5
        v_r = v_relative(v=v, met=met_rate)
        clo_d = clo_dynamic(clo=clo_insulation, met=met_rate)
        result = pmv(tdb=t_db, tr=t_r, vr=v_r, rh=relative_humidity, met=met_rate, clo=clo_d)


        new_pmv_data = PMVData(pmv=result, timestamp=latest_data.timestamp)  # 새 PMV 데이터 인스턴스 생성
        db.session.add(new_pmv_data)  # 세션에 추가
        db.session.commit()  # 데이터베이스에 커밋

        # 스프링 부트 애플리케이션 URL
        spring_boot_app_url = "http://172.30.1.2:8080/latestData"

        # 스프링 부트 애플리케이션에 PMV 값을 POST 요청으로 보냄
        response = requests.post(spring_boot_app_url, json={'pmv': result})

        response_data = {
            'temperature': latest_data.temperature,
            'humidity': latest_data.humidity,
            'pmv': result
        }

        print(response_data)
        return jsonify(response_data), 200
    else:
        return jsonify({'message': 'No sensor data available'}), 404







if __name__ == '__main__':
    with app.app_context():
        db.create_all()  # 모든 테이블을 데이터베이스에 생성
    app.run(debug=True, host='0.0.0.0')
