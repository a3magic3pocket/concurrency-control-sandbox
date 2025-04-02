import http from 'k6/http'
import { sleep, check } from 'k6'

const typeName = "naive"

export const options = {
    vus: 1000,
    duration: '5m',
    iterations: 3000,
}

export default function () {
    let res = http.get('http://web:8080/naive');
}

export function handleSummary(data) {
    const summary = {
        http_req_failed: data.metrics.http_req_failed,
        http_reqs: data.metrics.http_reqs,
        state: data.state,
        http_req_duration: data.metrics.http_req_duration,
    };

    return {
        'stdout': JSON.stringify(data, null, 2),
        [`/k6-result/${typeName}.json`]: JSON.stringify(summary, null, 2),
    };
}

