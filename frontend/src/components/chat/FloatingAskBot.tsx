import { useMemo, useState } from 'react';
import {
  Badge,
  Box,
  CircularProgress,
  Fab,
  IconButton,
  Paper,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import ChatOutlinedIcon from '@mui/icons-material/ChatOutlined';
import CloseIcon from '@mui/icons-material/Close';
import SendIcon from '@mui/icons-material/Send';
import SmartToyOutlinedIcon from '@mui/icons-material/SmartToyOutlined';
import { extractErrorMessage } from '@/api/axiosClient';
import { useAskAiMutation } from '@/api/aiApi';

interface ChatMessage {
  id: number;
  role: 'user' | 'assistant';
  content: string;
  meta?: string;
}

const initialMessages: ChatMessage[] = [
  {
    id: 1,
    role: 'assistant',
    content: 'Ask mode is ready. Ask me anything and I will answer using AI.',
  },
];

export function FloatingAskBot() {
  const [open, setOpen] = useState(false);
  const [input, setInput] = useState('');
  const [messages, setMessages] = useState<ChatMessage[]>(initialMessages);
  const askAiMutation = useAskAiMutation();

  const unreadHintCount = useMemo(
    () => Math.max(0, messages.filter((m) => m.role === 'assistant').length - 1),
    [messages]
  );

  const sendMessage = () => {
    const text = input.trim();
    if (!text || askAiMutation.isPending) return;

    const userMessage: ChatMessage = {
      id: Date.now(),
      role: 'user',
      content: text,
    };

    setMessages((prev) => [...prev, userMessage]);
    setInput('');
    setOpen(true);

    askAiMutation.mutate(
      { question: text },
      {
        onSuccess: (response) => {
          const assistantMessage: ChatMessage = {
            id: Date.now() + 1,
            role: 'assistant',
            content: response.answer,
            meta: response.model,
          };
          setMessages((prev) => [...prev, assistantMessage]);
        },
        onError: (error) => {
          const assistantMessage: ChatMessage = {
            id: Date.now() + 1,
            role: 'assistant',
            content: `I could not get an AI response: ${extractErrorMessage(error)}`,
          };
          setMessages((prev) => [...prev, assistantMessage]);
        },
      }
    );
  };

  return (
    <>
      <Box sx={{ position: 'fixed', right: 20, bottom: 20, zIndex: (theme) => theme.zIndex.modal + 1 }}>
        <Badge color="error" badgeContent={unreadHintCount > 9 ? '9+' : unreadHintCount} invisible={open || unreadHintCount === 0}>
          <Fab
            color="primary"
            aria-label="Open Ask AI"
            onClick={() => setOpen((prev) => !prev)}
            sx={{ boxShadow: 6 }}
          >
            {open ? <CloseIcon /> : <ChatOutlinedIcon />}
          </Fab>
        </Badge>
      </Box>

      {open && (
        <Paper
          elevation={10}
          sx={{
            position: 'fixed',
            right: 20,
            bottom: 88,
            width: { xs: 'calc(100vw - 32px)', sm: 380 },
            maxHeight: '65vh',
            display: 'flex',
            flexDirection: 'column',
            overflow: 'hidden',
            zIndex: (theme) => theme.zIndex.modal + 1,
          }}
        >
          <Box sx={{ px: 2, py: 1.5, bgcolor: 'primary.main', color: 'primary.contrastText', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <Stack direction="row" spacing={1} alignItems="center">
              <SmartToyOutlinedIcon fontSize="small" />
              <Typography variant="subtitle1" fontWeight={700}>Ask AI</Typography>
            </Stack>
            <IconButton size="small" onClick={() => setOpen(false)} sx={{ color: 'inherit' }}>
              <CloseIcon fontSize="small" />
            </IconButton>
          </Box>

          <Box sx={{ p: 1.5, overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: 1.2, bgcolor: 'background.default' }}>
            {messages.map((message) => (
              <Box
                key={message.id}
                sx={{
                  alignSelf: message.role === 'user' ? 'flex-end' : 'flex-start',
                  maxWidth: '88%',
                  px: 1.5,
                  py: 1,
                  borderRadius: 2,
                  bgcolor: message.role === 'user' ? 'primary.main' : 'grey.200',
                  color: message.role === 'user' ? 'primary.contrastText' : 'text.primary',
                }}
              >
                <Typography variant="body2">{message.content}</Typography>
                {message.meta ? (
                  <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 0.5 }}>
                    model: {message.meta}
                  </Typography>
                ) : null}
              </Box>
            ))}
            {askAiMutation.isPending ? (
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, px: 1 }}>
                <CircularProgress size={16} />
                <Typography variant="caption" color="text.secondary">AI is thinking...</Typography>
              </Box>
            ) : null}
          </Box>

          <Box sx={{ p: 1.25, borderTop: 1, borderColor: 'divider', display: 'flex', gap: 1 }}>
            <TextField
              size="small"
              fullWidth
              placeholder="Ask about workout, diet, notification..."
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter' && !e.shiftKey) {
                  e.preventDefault();
                  sendMessage();
                }
              }}
            />
            <IconButton color="primary" onClick={sendMessage} aria-label="Send AI question">
              <SendIcon />
            </IconButton>
          </Box>
        </Paper>
      )}
    </>
  );
}
