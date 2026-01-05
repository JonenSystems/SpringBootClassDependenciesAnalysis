/**
 * クラス図作成画面のJavaScript
 */
document.addEventListener('DOMContentLoaded', function() {
    // Mermaidの初期化
    mermaid.initialize({ 
        startOnLoad: false,
        theme: 'default',
        securityLevel: 'loose'
    });
    
    // クラス図をレンダリング
    const hiddenInput = document.getElementById('class-diagram-text');
    const mermaidElement = document.getElementById('mermaid-diagram');
    
    if (hiddenInput && mermaidElement && hiddenInput.value) {
        const diagramText = hiddenInput.value.trim();
        
        if (diagramText) {
            // Mermaidのテキストを設定
            mermaidElement.textContent = diagramText;
            
            // Mermaidでレンダリング
            mermaid.run({
                nodes: [mermaidElement]
            }).catch(function(error) {
                console.error('Mermaid rendering error:', error);
                mermaidElement.innerHTML = '<p class="text-danger">クラス図の表示中にエラーが発生しました: ' + error.message + '</p>';
            });
        }
    }
    
    // ダウンロードボタンの処理
    const downloadButton = document.getElementById('download-btn');
    if (downloadButton) {
        downloadButton.addEventListener('click', function() {
            // 隠しフィールドからクラス図テキストを取得
            if (hiddenInput && hiddenInput.value) {
                const classDiagramText = hiddenInput.value;
                const timestamp = new Date().toISOString().replace(/[:.]/g, '-').slice(0, -5);
                const fileName = `class-diagram-${timestamp}.mmd`;
                
                // Blobオブジェクトを作成
                const blob = new Blob([classDiagramText], { type: 'text/plain' });
                const url = URL.createObjectURL(blob);
                
                // ダウンロードリンクを作成してクリック
                const link = document.createElement('a');
                link.href = url;
                link.download = fileName;
                document.body.appendChild(link);
                link.click();
                document.body.removeChild(link);
                
                // URLを解放
                URL.revokeObjectURL(url);
            }
        });
    }
    
    // エンドポイント選択の保持
    const endpointSelect = document.getElementById('endpoint-select');
    const selectedEndpointIdInput = document.getElementById('selected-endpoint-id');
    const selectedEndpointUriInput = document.getElementById('selected-endpoint-uri');
    const selectedEndpointHttpMethodInput = document.getElementById('selected-endpoint-http-method');
    const selectedEndpointClassNameInput = document.getElementById('selected-endpoint-class-name');
    
    if (endpointSelect) {
        let selectedIndex = -1;
        
        // まず、エンドポイントIDで一致を試みる
        if (selectedEndpointIdInput && selectedEndpointIdInput.value) {
            const selectedId = selectedEndpointIdInput.value.trim();
            if (selectedId) {
                for (let i = 0; i < endpointSelect.options.length; i++) {
                    if (endpointSelect.options[i].value === selectedId) {
                        selectedIndex = i;
                        break;
                    }
                }
            }
        }
        
        // IDで一致しない場合、URI、HTTPメソッド、クラス名で一致を試みる
        if (selectedIndex === -1 && selectedEndpointUriInput && selectedEndpointHttpMethodInput && selectedEndpointClassNameInput) {
            const selectedUri = selectedEndpointUriInput.value.trim();
            const selectedHttpMethod = selectedEndpointHttpMethodInput.value.trim();
            const selectedClassName = selectedEndpointClassNameInput.value.trim();
            
            if (selectedUri && selectedHttpMethod && selectedClassName) {
                for (let i = 0; i < endpointSelect.options.length; i++) {
                    const optionText = endpointSelect.options[i].textContent;
                    // オプションテキストの形式: "/uri(HTTP_METHOD) : ClassName"
                    const match = optionText.match(/^(.+?)\((.+?)\)\s*:\s*(.+)$/);
                    if (match) {
                        const uri = match[1].trim();
                        const httpMethod = match[2].trim();
                        const className = match[3].trim();
                        
                        if (uri === selectedUri && httpMethod === selectedHttpMethod && className === selectedClassName) {
                            selectedIndex = i;
                            break;
                        }
                    }
                }
            }
        }
        
        // 一致するオプションが見つかった場合、選択する
        if (selectedIndex !== -1) {
            endpointSelect.selectedIndex = selectedIndex;
        }
    }
});

