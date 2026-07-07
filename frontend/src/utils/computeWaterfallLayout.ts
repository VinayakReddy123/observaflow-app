import { type TraceSpan } from '../types/trace';

export interface WaterfallSpan extends TraceSpan {
  leftPercent: number
  widthPercent: number
}


export function computeWaterfallLayout(spans: TraceSpan[]): WaterfallSpan[] {
  if (spans.length === 0) {
    return [];
  }
  const traceStart = Math.min(...spans.map((span)=>span.timestamp));
  const traceEnd = Math.max(...spans.map((span)=>span.timestamp + span.payload.duration));
  const totalDuration = traceEnd - traceStart;

  return spans.map((span) => {
    const leftPercent = ((span.timestamp - traceStart) / totalDuration) * 100;
    const widthPercent = (span.payload.duration / totalDuration) * 100; 
    return { ...span, leftPercent, widthPercent };
  });
}