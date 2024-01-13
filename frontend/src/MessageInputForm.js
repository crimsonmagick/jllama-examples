import React, {useState} from 'react';

export function MessageInputForm({isSubmitDisabled, handleFormSubmit}) {
  const [inputValue, setInputValue] = useState('');
  const [textAreaRows, setTextAreaRows] = useState(1);
  const MAX_ROW_NUMBER = 10;

  const handleInputTextChange = (event) => {
    updateTextBox(event.target.value);
  };

  const updateTextBox = (inputText) => {
    const numRows = inputText.split('\n').length;
    setTextAreaRows(numRows > MAX_ROW_NUMBER ? MAX_ROW_NUMBER : numRows);
    setInputValue(inputText);
  };

  const handleKeyDown = async (event) => {
    if (!isSubmitDisabled() && event.key === 'Enter' && event.ctrlKey) {
      event.preventDefault();
      updateTextBox('');
      await handleFormSubmit(event, inputValue);
    }
  };

  return (
    <form
      onSubmit={(event) => {
        handleFormSubmit(event, inputValue);
        updateTextBox('');
      }}
      className="form-container">
      <div className="input-wrapper">
        <textarea value={inputValue} onChange={handleInputTextChange} onKeyDown={handleKeyDown} rows={textAreaRows}/>
        <div className="button-wrapper">
          <button type="submit" disabled={isSubmitDisabled() || inputValue === null || inputValue.trim() === ''}><i className="fa fa-paper-plane"></i></button>
        </div>
      </div>
    </form>
  );
}
