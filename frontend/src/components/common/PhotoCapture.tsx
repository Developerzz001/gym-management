import { useEffect, useRef, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Typography,
} from '@mui/material';
import CameraAltOutlinedIcon from '@mui/icons-material/CameraAltOutlined';
import FileUploadOutlinedIcon from '@mui/icons-material/FileUploadOutlined';
import PersonOutlineIcon from '@mui/icons-material/PersonOutline';

interface PhotoCaptureProps {
  file: File | null;
  onChange: (file: File) => void;
  disabled?: boolean;
  height?: number;
}

const ACCEPTED_IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/webp'];
const MAX_IMAGE_SIZE = 5 * 1024 * 1024;

export function PhotoCapture({ file, onChange, disabled = false, height = 164 }: PhotoCaptureProps) {
  const videoRef = useRef<HTMLVideoElement>(null);
  const [previewUrl, setPreviewUrl] = useState<string>();
  const [cameraOpen, setCameraOpen] = useState(false);
  const [cameraReady, setCameraReady] = useState(false);
  const [cameraStream, setCameraStream] = useState<MediaStream | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    if (!file) {
      setPreviewUrl(undefined);
      return;
    }
    const url = URL.createObjectURL(file);
    setPreviewUrl(url);
    return () => URL.revokeObjectURL(url);
  }, [file]);

  useEffect(() => {
    if (videoRef.current && cameraStream) {
      videoRef.current.srcObject = cameraStream;
    }
  }, [cameraOpen, cameraStream]);

  useEffect(() => () => cameraStream?.getTracks().forEach((track) => track.stop()), [cameraStream]);

  const validateAndSelect = (selectedFile: File) => {
    if (!ACCEPTED_IMAGE_TYPES.includes(selectedFile.type)) {
      setErrorMessage('Only JPG, PNG, and WEBP images are supported');
      return;
    }
    if (selectedFile.size > MAX_IMAGE_SIZE) {
      setErrorMessage('Photo must be 5 MB or smaller');
      return;
    }
    setErrorMessage(null);
    onChange(selectedFile);
  };

  const stopCamera = () => {
    cameraStream?.getTracks().forEach((track) => track.stop());
    setCameraStream(null);
    setCameraOpen(false);
    setCameraReady(false);
  };

  const openCamera = async () => {
    setErrorMessage(null);
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ video: { facingMode: 'user' }, audio: false });
      setCameraStream(stream);
      setCameraOpen(true);
    } catch {
      setErrorMessage('Camera access was unavailable. Check browser permissions and try again.');
    }
  };

  const capturePhoto = () => {
    const video = videoRef.current;
    if (!video || !cameraReady || !video.videoWidth || !video.videoHeight) {
      setErrorMessage('Camera is still starting. Please try again in a moment.');
      return;
    }

    const canvas = document.createElement('canvas');
    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;
    const context = canvas.getContext('2d');
    if (!context) {
      setErrorMessage('Photo could not be captured. Please try again.');
      return;
    }
    context.translate(canvas.width, 0);
    context.scale(-1, 1);
    context.drawImage(video, 0, 0);
    canvas.toBlob((blob) => {
      if (blob) {
        validateAndSelect(new File([blob], `client-photo-${Date.now()}.jpg`, { type: 'image/jpeg' }));
        stopCamera();
      } else {
        setErrorMessage('Photo could not be captured. Please try again.');
      }
    }, 'image/jpeg', 0.9);
  };

  return (
    <Box>
      <Box
        sx={{
          height,
          border: '1px solid',
          borderColor: 'divider',
          borderRadius: 1,
          bgcolor: 'action.hover',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          overflow: 'hidden',
        }}
      >
        {cameraOpen ? (
          <Box
            component="video"
            ref={videoRef}
            autoPlay
            playsInline
            muted
            onLoadedMetadata={(event) => {
              event.currentTarget.play().then(() => setCameraReady(true)).catch(() => {
                setErrorMessage('Camera preview could not be started.');
              });
            }}
            sx={{ width: '100%', height: '100%', objectFit: 'cover', transform: 'scaleX(-1)' }}
          />
        ) : previewUrl ? (
          <Box component="img" src={previewUrl} alt="Selected client" sx={{ width: '100%', height: '100%', objectFit: 'cover' }} />
        ) : (
          <Box sx={{ textAlign: 'center', color: 'text.secondary' }}>
            <PersonOutlineIcon sx={{ fontSize: 54 }} />
            <Typography variant="body2">Photo</Typography>
          </Box>
        )}
      </Box>
      <Box sx={{ display: 'flex', gap: 1, mt: 1 }}>
        <Button
          fullWidth
          size="small"
          variant="outlined"
          startIcon={<CameraAltOutlinedIcon />}
          onClick={cameraOpen ? capturePhoto : openCamera}
          disabled={disabled || (cameraOpen && !cameraReady)}
        >
          {cameraOpen ? (cameraReady ? 'Take Photo' : 'Starting...') : 'Capture'}
        </Button>
        {cameraOpen ? (
          <Button fullWidth size="small" variant="outlined" onClick={stopCamera} disabled={disabled}>Cancel</Button>
        ) : (
          <Button fullWidth component="label" size="small" variant="outlined" startIcon={<FileUploadOutlinedIcon />} disabled={disabled}>
            Upload
            <input hidden type="file" accept="image/jpeg,image/png,image/webp" onChange={(event) => {
              const selectedFile = event.target.files?.[0];
              if (selectedFile) validateAndSelect(selectedFile);
              event.target.value = '';
            }} />
          </Button>
        )}
      </Box>
      {errorMessage && <Alert severity="error" sx={{ mt: 1 }}>{errorMessage}</Alert>}
    </Box>
  );
}