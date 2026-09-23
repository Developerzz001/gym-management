import { useEffect, useRef, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Typography,
} from '@mui/material';
import CameraAltOutlinedIcon from '@mui/icons-material/CameraAltOutlined';
import FileUploadOutlinedIcon from '@mui/icons-material/FileUploadOutlined';
import PersonOutlineIcon from '@mui/icons-material/PersonOutline';

interface PhotoCaptureProps {
  file: File | null;
  onChange: (file: File) => void;
  disabled?: boolean;
}

const ACCEPTED_IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/webp'];
const MAX_IMAGE_SIZE = 5 * 1024 * 1024;

export function PhotoCapture({ file, onChange, disabled = false }: PhotoCaptureProps) {
  const videoRef = useRef<HTMLVideoElement>(null);
  const [previewUrl, setPreviewUrl] = useState<string>();
  const [cameraOpen, setCameraOpen] = useState(false);
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
    if (!video || !video.videoWidth || !video.videoHeight) return;

    const canvas = document.createElement('canvas');
    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;
    canvas.getContext('2d')?.drawImage(video, 0, 0);
    canvas.toBlob((blob) => {
      if (blob) {
        validateAndSelect(new File([blob], `client-photo-${Date.now()}.jpg`, { type: 'image/jpeg' }));
        stopCamera();
      }
    }, 'image/jpeg', 0.9);
  };

  return (
    <Box>
      <Box
        sx={{
          height: 164,
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
        {previewUrl ? (
          <Box component="img" src={previewUrl} alt="Selected client" sx={{ width: '100%', height: '100%', objectFit: 'cover' }} />
        ) : (
          <Box sx={{ textAlign: 'center', color: 'text.secondary' }}>
            <PersonOutlineIcon sx={{ fontSize: 54 }} />
            <Typography variant="body2">Photo</Typography>
          </Box>
        )}
      </Box>
      <Box sx={{ display: 'flex', gap: 1, mt: 1 }}>
        <Button fullWidth size="small" variant="outlined" startIcon={<CameraAltOutlinedIcon />} onClick={openCamera} disabled={disabled}>
          Capture
        </Button>
        <Button fullWidth component="label" size="small" variant="outlined" startIcon={<FileUploadOutlinedIcon />} disabled={disabled}>
          Upload
          <input hidden type="file" accept="image/jpeg,image/png,image/webp" onChange={(event) => {
            const selectedFile = event.target.files?.[0];
            if (selectedFile) validateAndSelect(selectedFile);
            event.target.value = '';
          }} />
        </Button>
      </Box>
      {errorMessage && <Alert severity="error" sx={{ mt: 1 }}>{errorMessage}</Alert>}

      <Dialog open={cameraOpen} onClose={stopCamera} maxWidth="sm" fullWidth>
        <DialogTitle>Capture Photo</DialogTitle>
        <DialogContent dividers sx={{ p: 0, bgcolor: 'common.black' }}>
          <Box component="video" ref={videoRef} autoPlay playsInline muted sx={{ display: 'block', width: '100%', maxHeight: '65vh' }} />
        </DialogContent>
        <DialogActions>
          <Button onClick={stopCamera}>Cancel</Button>
          <Button variant="contained" startIcon={<CameraAltOutlinedIcon />} onClick={capturePhoto}>Capture</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}