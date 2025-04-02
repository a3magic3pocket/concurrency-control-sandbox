import http from 'k6/http'
import { sleep, check } from 'k6'

const typeName = "pessimistic-lock-with-retrieving"
const printError = false

export const options = {
    vus: 100,
    duration: '5m',
    iterations: 3000,
    thresholds: {
        'http_req_failed{group:test}': ['rate>=0'],
        'http_req_duration{group:test}': ['max>=0'],
        'http_req_waiting{group:test}': ['max>=0'],
        'http_reqs{group:test}': ['rate>=0'],
    }
}

export function setup() {
    let result = {
        postId: -1
    }

    let res = http.post('http://web:8080/post')
    if (Math.floor(res.status / 100) !== 2) {
        console.error("res: ", res)
        throw new Error("failed to setup")
    }

    const data = res.json()
    if (data && data.id) {
        result.postId = data.id
    }

    return result
}

export default function (data) {
    const { postId } = data

    let res = http.get(`http://web:8080/posts`, {
        tags: {
            group: 'test',
        },
    });
    if (printError && Math.floor(res.status / 100) !== 2 && res.body) {
        console.error("res.body", res.body)
    }

    const postParams = {
        headers: {
            'Content-Type': 'application/json',
        },
        tags: {
            group: 'test',
        },
    };

    res = http.post(`http://web:8080/post/${postId}/like`, {}, postParams);
    if (printError && Math.floor(res.status / 100) !== 2 && res.body) {
        console.error("res.body", res.body)
    }
}

export function handleSummary(data) {
    const summary = {
        http_req_failed: data.metrics["http_req_failed{group:test}"],
        http_reqs: data.metrics["http_reqs{group:test}"],
        state: data.state,
        http_req_duration: data.metrics["http_req_duration{group:test}"],
    }

    return {
        'stdout': JSON.stringify(data, null, 2),
        [`/k6-result/${typeName}.json`]: JSON.stringify(summary, null, 2),
    };
}

