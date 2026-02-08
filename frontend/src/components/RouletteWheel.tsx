import { cn } from '@/lib/utils';
import { useState, useEffect } from 'react';

interface RouletteWheelProps {
  isSpinning: boolean;
  targetAmount?: number;
  onSpinComplete?: () => void;
}

const SECTIONS = [
  { label: '100', value: 100, color: '#8B5CF6' },
  { label: '200', value: 200, color: '#A78BFA' },
  { label: '300', value: 300, color: '#C084FC' },
  { label: '400', value: 400, color: '#EC4899' },
  { label: '500', value: 500, color: '#F472B6' },
  { label: '600', value: 600, color: '#F9A8D4' },
  { label: '700', value: 700, color: '#7C3AED' },
  { label: '800', value: 800, color: '#9333EA' },
  { label: '900', value: 900, color: '#A855F7' },
  { label: '1000', value: 1000, color: '#D946EF' },
];

export default function RouletteWheel({
  isSpinning,
  targetAmount,
  onSpinComplete
}: RouletteWheelProps) {
  const [rotation, setRotation] = useState(0);

  useEffect(() => {
    if (isSpinning && targetAmount !== undefined) {
      // 포인터는 상단(12시 방향)에 고정
      // targetIndex 섹션을 상단으로 가져오기 위한 회전 각도 계산
      const targetIndex = SECTIONS.findIndex(s => s.value === targetAmount);
      const baseRotation = 720; // 2바퀴
      const sectionAngle = 360 / SECTIONS.length;

      // 반시계방향으로 회전하여 targetIndex 섹션을 상단으로
      // (섹션이 시계방향으로 배열되어 있으므로 음수로 회전)
      const targetAngle = -(targetIndex * sectionAngle);

      // 섹션 내에서 랜덤 위치
      const randomOffset = (Math.random() - 0.5) * sectionAngle * 0.6;

      const finalRotation = baseRotation + targetAngle + randomOffset;
      setRotation(finalRotation);

      // 애니메이션 완료 후 콜백
      const timer = setTimeout(() => {
        onSpinComplete?.();
      }, 3000);

      return () => clearTimeout(timer);
    }
  }, [isSpinning, targetAmount, onSpinComplete]);

  const sectionAngle = 360 / SECTIONS.length;

  return (
    <div className="relative w-80 h-80 mx-auto">
      {/* 상단 포인터 */}
      <div className="absolute top-0 left-1/2 -translate-x-1/2 -translate-y-2 z-10">
        <div className="w-0 h-0 border-l-[15px] border-l-transparent border-r-[15px] border-r-transparent border-t-[25px] border-t-red-500 drop-shadow-lg" />
      </div>

      {/* 룰렛 휠 */}
      <div
        className={cn(
          "relative w-full h-full rounded-full shadow-2xl",
          "transition-transform ease-out",
          "backdrop-blur-sm bg-white/80"
        )}
        style={{
          transform: `rotate(${rotation}deg)`,
          transitionDuration: '3000ms',
        }}
      >
        <svg
          viewBox="0 0 100 100"
          className="w-full h-full"
          style={{ transform: 'rotate(-90deg)' }}
        >
          {SECTIONS.map((section, index) => {
            const startAngle = sectionAngle * index;
            const endAngle = startAngle + sectionAngle;

            const startRadians = (startAngle * Math.PI) / 180;
            const endRadians = (endAngle * Math.PI) / 180;

            const x1 = 50 + 50 * Math.cos(startRadians);
            const y1 = 50 + 50 * Math.sin(startRadians);
            const x2 = 50 + 50 * Math.cos(endRadians);
            const y2 = 50 + 50 * Math.sin(endRadians);

            const largeArcFlag = sectionAngle > 180 ? 1 : 0;

            const pathData = [
              `M 50 50`,
              `L ${x1} ${y1}`,
              `A 50 50 0 ${largeArcFlag} 1 ${x2} ${y2}`,
              'Z'
            ].join(' ');

            // 텍스트 위치 계산
            const textAngle = startAngle + sectionAngle / 2;
            const textRadians = (textAngle * Math.PI) / 180;
            const textX = 50 + 35 * Math.cos(textRadians);
            const textY = 50 + 35 * Math.sin(textRadians);

            return (
              <g key={index}>
                <path d={pathData} fill={section.color} />
                <text
                  x={textX}
                  y={textY}
                  textAnchor="middle"
                  dominantBaseline="middle"
                  fill="white"
                  fontSize="4"
                  fontWeight="bold"
                  transform={`rotate(${textAngle + 90}, ${textX}, ${textY})`}
                >
                  {section.label}
                </text>
              </g>
            );
          })}
        </svg>

        {/* 중앙 원 */}
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-20 h-20 rounded-full bg-gradient-to-br from-purple-500 to-pink-500 shadow-lg flex items-center justify-center border-4 border-white">
          <div className="text-3xl">🎰</div>
        </div>
      </div>
    </div>
  );
}
