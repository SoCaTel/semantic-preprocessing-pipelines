import globals from "../../app-service/globals";
import {fetchJson} from "../../app-service/http";

export async function fetchDebugMetadata(
    execution: string, path: string, source: string,
    offset: number, limit: number) {
    let url = getMetadataUrl(execution, path);
    url += "?offset=" + offset + "&limit=" + limit;
    if (source) {
        url += "&source=" + source;
    }
    return await fetchJson(url);
}

function getMetadataUrl(execution: string, path: string): string {
    if (path.length > 0 && path[0] !== "/") {
        path = "/" + path
    }
    return globals.api_gate_url + "api/v1/debug/metadata/" + execution + path;
}

export function getDownloadDebugUrl(
    execution: string, path: string, source: string) {
    let url = globals.api_gate_url + "api/v1/debug/data/" + execution + path;
    if (source) {
        url += "?source=" + source;
    }
    return url;
}