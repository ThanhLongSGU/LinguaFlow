import api from './api';

export const logScan = async (qrId: number) => {
  const response = await api.post('/scan-logs', { qrId });
  return response.data;
};

export const getScanLogs = async (page = 0, size = 10) => {
  const response = await api.get(`/scan-logs?page=${page}&size=${size}`);
  return response.data;
};