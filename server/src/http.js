/** HTTP yordamchilari. */

export class ApiError extends Error {
  constructor(status, message, details = undefined) {
    super(message);
    this.status = status;
    this.details = details;
  }
}

export const badRequest = (message, details) => new ApiError(400, message, details);
export const unauthorized = (message = 'Avtorizatsiyadan o\'tmagan') => new ApiError(401, message);
export const forbidden = (message = 'Ruxsat yo\'q') => new ApiError(403, message);
export const notFound = (message = 'Topilmadi') => new ApiError(404, message);
export const conflict = (message) => new ApiError(409, message);
export const notImplemented = (message) => new ApiError(501, message);

/** Express 4 async xatoliklarini ushlash. */
export const asyncHandler = (fn) => (req, res, next) => {
  Promise.resolve(fn(req, res, next)).catch(next);
};

export function errorHandler(error, _req, res, _next) {
  const status = error.status || 500;
  if (status >= 500) {
    console.error('[api]', error);
  }
  res.status(status).json({
    error: {
      message: status >= 500 ? 'Server xatoligi' : error.message,
      status,
      details: error.details,
    },
  });
}
