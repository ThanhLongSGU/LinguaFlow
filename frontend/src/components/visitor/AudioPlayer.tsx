import React, { useRef, useState } from 'react';
import { Play, Pause, RotateCcw } from 'lucide-react';

interface AudioPlayerProps {
  audioUrl: string;
}

export const AudioPlayer: React.FC<AudioPlayerProps> = ({ audioUrl }) => {
  const audioRef = useRef<HTMLAudioElement | null>(null);
  const [isPlaying, setIsPlaying] = useState(false);
  const [progress, setProgress] = useState(0);

  const togglePlay = () => {
    if (!audioRef.current) return;
    if (isPlaying) {
      audioRef.current.pause();
    } else {
      audioRef.current.play();
    }
    setIsPlaying(!isPlaying);
  };

  const handleTimeUpdate = () => {
    if (!audioRef.current) return;
    const current = audioRef.current.currentTime;
    const duration = audioRef.current.duration || 1;
    setProgress((current / duration) * 100);
  };

  return (
    <div className="fixed bottom-0 left-0 right-0 bg-white/95 backdrop-blur border-t p-4 shadow-xl flex items-center gap-4">
      <audio
        ref={audioRef}
        src={audioUrl}
        onTimeUpdate={handleTimeUpdate}
        onEnded={() => setIsPlaying(false)}
      />
      <button
        onClick={togglePlay}
        className="w-12 h-12 bg-blue-600 text-white rounded-full flex items-center justify-center shadow hover:bg-blue-700"
      >
        {isPlaying ? <Pause className="w-6 h-6" /> : <Play className="w-6 h-6 ml-0.5" />}
      </button>

      <div className="flex-1">
        <div className="w-full bg-gray-200 h-2 rounded-full overflow-hidden">
          <div className="bg-blue-600 h-full transition-all duration-200" style={{ width: `${progress}%` }} />
        </div>
      </div>
    </div>
  );
};