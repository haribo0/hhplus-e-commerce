import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    stages: [
        { duration: '1m', target: 500 },   // 1분 동안 500명까지 증가
        { duration: '1m', target: 1000 },  // 1분 동안 1000명까지 증가
        { duration: '1m', target: 5000 },  // 1분 동안 5000명까지 증가
        { duration: '1m', target: 10000 }, // 1분 동안 10000명까지 증가
        { duration: '1m', target: 1000 },  // 1분 동안 1000명까지 감소
        { duration: '1m', target: 0 },     // 1분 동안 부하 감소
    ],
    thresholds: {
        http_req_duration: ['p(95)<1000'], // 95%의 요청이 1000ms 이하
        http_req_failed: ['rate<0.02'],   // 실패율 2% 미만
    }
};

export default function () {
    let userId = Math.floor(Math.random() * 25000) + 1; // 1 ~ 25000 랜덤 유저 ID
    let couponPolicyId = Math.random() < 0.5 ? 1 : 2; // 1 또는 2 중 랜덤 선택

    let res = http.post('http://localhost:8080/api/v1/coupons/request', JSON.stringify({
        userId: userId,
        couponPolicyId: couponPolicyId
    }), {
        headers: { 'Content-Type': 'application/json' }
    });

    let isValid = check(res, {
        '쿠폰 발급 성공': (r) => r.status === 201,
        '예외처리 메세지 반환': (r) => r.status === 400,
    });

    if (!isValid) {
        console.error(`Unexpected status code: ${res.status}`);
    }


    sleep(1); // 1초 대기 후 다음 요청
}
